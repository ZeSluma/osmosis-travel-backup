package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Software-only KA fault matrix. It deliberately proves state boundaries, never Pocket timing,
 * power state or a model-specific wake/keepalive mechanism.
 */
class LivenessFaultMatrixTest {
    @Test fun transientLossesAreBoundedAndAReadySessionStillNeedsFreshRevalidation() {
        var state = RecoverySnapshot(ConnectionState.READY)
        repeat(RecoveryStateMachine.MAX_TRANSIENT_ATTEMPTS) { attempt ->
            state = RecoveryStateMachine.reduce(state, ConnectionEvent.LOST, ConnectionReason.NETWORK_LOSS)
            assertEquals(ConnectionState.RECONNECT_WAIT, state.state)
            assertEquals(attempt + 1, state.attempts)
            state = RecoveryStateMachine.reduce(state, ConnectionEvent.RETRY_TIMER)
            assertEquals(ConnectionState.RECONNECTING, state.state)
            state = RecoveryStateMachine.reduce(state, ConnectionEvent.TRANSPORT_READY)
            assertEquals(ConnectionState.REVALIDATING, state.state)
            // A transport event is intentionally insufficient to restore camera traffic.
            assertFalse(CameraSessionCoordinator.mayUseCameraTraffic(SessionLease(1, state)))
        }
        state = RecoveryStateMachine.reduce(state, ConnectionEvent.LOST, ConnectionReason.SESSION_DESYNC)
        assertEquals(ConnectionState.USER_ACTION_REQUIRED, state.state)
        assertEquals(ConnectionReason.SESSION_DESYNC, state.reason)
    }

    @Test fun permanentCauseAndExplicitStopNeverScheduleAnotherRecovery() {
        val unavailable = RecoveryStateMachine.reduce(
            RecoverySnapshot(ConnectionState.CONNECTING), ConnectionEvent.LOST, ConnectionReason.CAMERA_UNAVAILABLE)
        assertEquals(ConnectionState.USER_ACTION_REQUIRED, unavailable.state)
        assertEquals(ConnectionReason.CAMERA_UNAVAILABLE, unavailable.reason)

        val stopped = RecoveryStateMachine.reduce(unavailable, ConnectionEvent.STOP)
        assertEquals(ConnectionState.STOPPED, stopped.state)
        assertEquals(stopped, RecoveryStateMachine.reduce(stopped, ConnectionEvent.RETRY_TIMER))
        assertEquals(stopped, RecoveryStateMachine.reduce(stopped, ConnectionEvent.TRANSPORT_READY))
    }

    @Test fun recoveryScanPolicyDoesNotInferCameraPowerFromARecoverableLoss() {
        val transient = SessionLease(9, RecoverySnapshot(ConnectionState.RECONNECT_WAIT, 1, ConnectionReason.NETWORK_LOSS))
        assertTrue(CameraRecoveryScanPolicy.mayScheduleAfterLoss(transient))
        assertEquals(1, CameraRecoveryScanPolicy.nextAttempt(0, transient))
        assertFalse(CameraRecoveryScanPolicy.mayScheduleAfterLoss(transient.copy(userStopped = true)))
        assertEquals(null, CameraRecoveryScanPolicy.nextAttempt(0, transient.copy(userStopped = true)))
    }
}
