package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DurableSessionRuntimeTest {
    private class MemoryStore(var value: SessionLease = SessionLease()) : SessionStore {
        private var transfer: TransferLease? = null
        private var generation = 0L
        override fun read() = value
        override fun write(value: SessionLease) { this.value = value }
        override fun readTransfer() = transfer
        override fun writeTransfer(value: TransferLease?) { transfer = value }
        override fun nextTransferGeneration() = ++generation
    }

    @Test fun processRestartRetainsFenceAndDoesNotTreatIncompleteEnumerationAsReady() {
        val store = MemoryStore()
        val first = DurableSessionRuntime(store)
        val lease = first.start()
        first.callback(lease.epoch, ConnectionEvent.TRANSPORT_READY)
        first.callback(lease.epoch, ConnectionEvent.LOST, ConnectionReason.NETWORK_LOSS)
        first.callback(lease.epoch, ConnectionEvent.RETRY_TIMER)
        // Simulate process death: only durable data is passed to the replacement runtime.
        val replacement = DurableSessionRuntime(store)
        assertEquals(ConnectionState.RECONNECTING, replacement.snapshot().recovery.state)
        replacement.callback(lease.epoch, ConnectionEvent.TRANSPORT_READY)
        assertEquals(SourceTrust.INCOMPLETE_UNTRUSTED,
            replacement.revalidated(lease.epoch, SourceObservation(enumerationComplete = false, enumerationFailed = false)))
        assertEquals(ConnectionState.REVALIDATING, replacement.snapshot().recovery.state)
        assertFalse(CameraSessionCoordinator.mayUseCameraTraffic(replacement.snapshot()))
        assertEquals(SourceTrust.TRUSTED,
            replacement.revalidated(lease.epoch, SourceObservation(enumerationComplete = true, enumerationFailed = false)))
        assertTrue(CameraSessionCoordinator.mayUseCameraTraffic(replacement.snapshot()))
    }

    @Test fun replacementEpochAndExplicitStopFenceEveryLateCallback() {
        val store = MemoryStore()
        val runtime = DurableSessionRuntime(store)
        val old = runtime.start()
        val new = runtime.start()
        runtime.callback(old.epoch, ConnectionEvent.SESSION_READY)
        assertEquals(new.epoch, runtime.snapshot().epoch)
        runtime.stop()
        val stopped = runtime.snapshot()
        runtime.callback(stopped.epoch, ConnectionEvent.TRANSPORT_READY)
        runtime.callback(stopped.epoch, ConnectionEvent.LOST, ConnectionReason.NETWORK_LOSS)
        assertEquals(stopped, runtime.snapshot())
    }

    @Test fun repeatedTransientFailureConsumesBoundedBudget() {
        val runtime = DurableSessionRuntime(MemoryStore())
        val epoch = runtime.start().epoch
        repeat(RecoveryStateMachine.MAX_TRANSIENT_ATTEMPTS + 1) {
            runtime.callback(epoch, ConnectionEvent.LOST, ConnectionReason.SESSION_DESYNC)
            runtime.callback(epoch, ConnectionEvent.RETRY_TIMER)
        }
        assertEquals(ConnectionState.USER_ACTION_REQUIRED, runtime.snapshot().recovery.state)
    }

    @Test fun transferAllocationIsSingleWriterAndStaleReleaseCannotFreeReplacement() {
        val runtime = DurableSessionRuntime(MemoryStore())
        val epoch = runtime.start().epoch
        runtime.callback(epoch, ConnectionEvent.TRANSPORT_READY)
        runtime.revalidated(epoch, SourceObservation(enumerationComplete = true, enumerationFailed = false))
        val first = checkNotNull(runtime.acquireTransfer(epoch))
        assertEquals(null, runtime.acquireTransfer(epoch))
        runtime.releaseTransfer(first)
        val second = checkNotNull(runtime.acquireTransfer(epoch))
        runtime.releaseTransfer(first)
        assertEquals(second, runtime.activeTransfer())
        runtime.releaseTransfer(second)
        assertEquals(null, runtime.activeTransfer())
    }

    @Test fun processReplacementReleasesOnlyTheInMemoryWriterNotTheDurablePartial() {
        val store = MemoryStore()
        val first = DurableSessionRuntime(store)
        val epoch = first.start().epoch
        first.callback(epoch, ConnectionEvent.TRANSPORT_READY)
        first.revalidated(epoch, SourceObservation(true, false))
        checkNotNull(first.acquireTransfer(epoch))
        assertTrue(store.readTransfer() != null)
        val replacement = DurableSessionRuntime(store)
        assertEquals(null, replacement.activeTransfer())
        assertEquals(null, store.readTransfer())
    }
}
