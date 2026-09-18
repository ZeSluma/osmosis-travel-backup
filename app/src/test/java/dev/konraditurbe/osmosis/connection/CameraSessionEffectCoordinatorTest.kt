package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraSessionEffectCoordinatorTest {
    private class Store : SessionStore {
        var session = SessionLease(); var transfer: TransferLease? = null; var generation = 0L
        override fun read() = session
        override fun write(value: SessionLease) { session = value }
        override fun readTransfer() = transfer
        override fun writeTransfer(value: TransferLease?) { transfer = value }
        override fun nextTransferGeneration() = ++generation
    }

    @Test fun coordinatorFencesStaleTransportAndRequiresFreshRevalidation() {
        val coordinator = CameraSessionEffectCoordinator(DurableSessionRuntime(Store()))
        val old = coordinator.begin()
        val current = coordinator.begin()
        coordinator.transportReady(old.epoch)
        assertEquals(ConnectionState.CONNECTING, coordinator.snapshot().recovery.state)
        coordinator.transportReady(current.epoch)
        assertEquals(SourceTrust.INCOMPLETE_UNTRUSTED, coordinator.revalidate(current.epoch, false, false))
        assertFalse(CameraSessionCoordinator.mayUseCameraTraffic(coordinator.snapshot()))
        assertEquals(SourceTrust.TRUSTED, coordinator.revalidate(current.epoch, true, false))
        assertTrue(CameraSessionCoordinator.mayUseCameraTraffic(coordinator.snapshot()))
        coordinator.stop()
        coordinator.transportLost(current.epoch, ConnectionReason.NETWORK_LOSS)
        assertTrue(coordinator.snapshot().userStopped)
    }
}
