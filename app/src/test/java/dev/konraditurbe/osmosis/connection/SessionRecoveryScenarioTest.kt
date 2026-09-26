package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRecoveryScenarioTest {
    private class Store(var value: SessionLease = SessionLease()) : SessionStore {
        private var transfer: TransferLease? = null
        private var generation = 0L
        override fun read() = value
        override fun write(value: SessionLease) { this.value = value }
        override fun readTransfer() = transfer
        override fun writeTransfer(value: TransferLease?) { transfer = value }
        override fun nextTransferGeneration() = ++generation
    }
    @Test fun activeSessionFailsClosedAcrossLossStaleCallbackAndIncompleteRevalidation() {
        var session = CameraSessionCoordinator.begin(SessionLease())
        val epoch = session.epoch
        session = CameraSessionCoordinator.event(session, epoch, ConnectionEvent.TRANSPORT_READY)
        session = CameraSessionCoordinator.event(session, epoch, ConnectionEvent.SESSION_READY)
        assertTrue(CameraSessionCoordinator.mayUseCameraTraffic(session))
        session = CameraSessionCoordinator.event(session, epoch, ConnectionEvent.LOST, ConnectionReason.NETWORK_LOSS)
        session = CameraSessionCoordinator.event(session, epoch, ConnectionEvent.RETRY_TIMER)
        assertTrue(CameraSessionCoordinator.mayRebuild(session))
        val replacement = CameraSessionCoordinator.begin(session)
        assertEquals(replacement, CameraSessionCoordinator.event(replacement, epoch, ConnectionEvent.REVALIDATED))
        assertFalse(CameraSessionCoordinator.mayUseCameraTraffic(replacement))
        val stopped = CameraSessionCoordinator.stop(replacement)
        assertEquals(stopped, CameraSessionCoordinator.event(stopped, stopped.epoch, ConnectionEvent.SESSION_READY))
    }
    @Test fun replacementEpochCannotUseAnIncompleteSourceOrReviveAStoppedWriter() {
        val runtime = DurableSessionRuntime(Store())
        val old = runtime.start()
        runtime.callback(old.epoch, ConnectionEvent.TRANSPORT_READY)
        assertEquals(SourceTrust.TRUSTED, runtime.revalidated(old.epoch, SourceObservation(true, false)))
        val oldWriter = checkNotNull(runtime.acquireTransfer(old.epoch))

        runtime.callback(old.epoch, ConnectionEvent.LOST, ConnectionReason.NETWORK_LOSS)
        runtime.callback(old.epoch, ConnectionEvent.RETRY_TIMER)
        val replacement = runtime.start()
        assertTrue(replacement.epoch > old.epoch)
        runtime.callback(old.epoch, ConnectionEvent.REVALIDATED)
        assertEquals(replacement.epoch, runtime.snapshot().epoch)

        runtime.callback(replacement.epoch, ConnectionEvent.TRANSPORT_READY)
        assertEquals(SourceTrust.INCOMPLETE_UNTRUSTED,
            runtime.revalidated(replacement.epoch, SourceObservation(false, false)))
        assertFalse(CameraSessionCoordinator.mayUseCameraTraffic(runtime.snapshot()))
        assertEquals(null, runtime.acquireTransfer(replacement.epoch))

        assertEquals(SourceTrust.TRUSTED,
            runtime.revalidated(replacement.epoch, SourceObservation(true, false)))
        // A replacement cannot overlap the fenced old writer.  The cancelled old worker releases
        // only its own token; only then may the fresh trusted epoch acquire its writer.
        assertEquals(null, runtime.acquireTransfer(replacement.epoch))
        runtime.releaseTransfer(oldWriter)
        val replacementWriter = checkNotNull(runtime.acquireTransfer(replacement.epoch))
        assertEquals(null, runtime.acquireTransfer(replacement.epoch))
        runtime.stop()
        runtime.releaseTransfer(replacementWriter)
        runtime.callback(replacement.epoch, ConnectionEvent.SESSION_READY)
        assertTrue(runtime.snapshot().userStopped)
        assertEquals(ConnectionState.STOPPED, runtime.snapshot().recovery.state)
        assertEquals(null, runtime.acquireTransfer(replacement.epoch))
    }

    @Test fun trustedReplacementIsWokenOnlyAfterTheFencedPredecessorReleasesItsWriter() {
        val runtime = DurableSessionRuntime(Store())
        val first = runtime.start()
        runtime.callback(first.epoch, ConnectionEvent.TRANSPORT_READY)
        assertEquals(SourceTrust.TRUSTED, runtime.revalidated(first.epoch, SourceObservation(true, false)))
        val predecessor = checkNotNull(runtime.acquireTransfer(first.epoch))

        val replacement = runtime.start()
        runtime.callback(replacement.epoch, ConnectionEvent.TRANSPORT_READY)
        assertEquals(SourceTrust.TRUSTED, runtime.revalidated(replacement.epoch, SourceObservation(true, false)))
        assertEquals(null, runtime.acquireTransfer(replacement.epoch))

        val releases = mutableListOf<TransferLease>()
        runtime.observeTransferRelease { releases += it }
        runtime.releaseTransfer(predecessor)

        assertEquals(listOf(predecessor), releases)
        val replacementWriter = checkNotNull(runtime.acquireTransfer(replacement.epoch))
        runtime.releaseTransfer(replacementWriter)
    }
}
