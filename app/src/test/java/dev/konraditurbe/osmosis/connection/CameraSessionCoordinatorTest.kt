package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraSessionCoordinatorTest {
    @Test fun staleCallbackCannotReplaceNewOwner() {
        val first = CameraSessionCoordinator.begin(SessionLease())
        val second = CameraSessionCoordinator.begin(first)
        val result = CameraSessionCoordinator.event(second, first.epoch, ConnectionEvent.SESSION_READY)
        assertEquals(second, result)
    }

    @Test fun transientLossRequiresCurrentEpochAndExplicitRebuild() {
        var s = CameraSessionCoordinator.begin(SessionLease())
        s = CameraSessionCoordinator.event(s, s.epoch, ConnectionEvent.TRANSPORT_READY)
        s = CameraSessionCoordinator.event(s, s.epoch, ConnectionEvent.SESSION_READY)
        assertTrue(CameraSessionCoordinator.mayUseCameraTraffic(s))
        s = CameraSessionCoordinator.event(s, s.epoch, ConnectionEvent.LOST, ConnectionReason.NETWORK_LOSS)
        assertFalse(CameraSessionCoordinator.mayUseCameraTraffic(s))
        s = CameraSessionCoordinator.event(s, s.epoch, ConnectionEvent.RETRY_TIMER)
        assertTrue(CameraSessionCoordinator.mayRebuild(s))
    }

    @Test fun userStopCannotBeResurrectedByLateNetworkCallback() {
        val running = CameraSessionCoordinator.begin(SessionLease())
        val stopped = CameraSessionCoordinator.stop(running)
        val late = CameraSessionCoordinator.event(stopped, stopped.epoch, ConnectionEvent.TRANSPORT_READY)
        assertEquals(stopped, late)
        assertFalse(CameraSessionCoordinator.mayRebuild(late))
    }
}