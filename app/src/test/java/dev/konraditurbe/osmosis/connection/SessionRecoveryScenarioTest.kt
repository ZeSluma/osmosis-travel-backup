package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRecoveryScenarioTest {
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
}