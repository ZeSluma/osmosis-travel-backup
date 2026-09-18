package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryStateMachineTest {
    @Test fun transientLossIsBoundedAndRequiresRevalidationBeforeReady() {
        var s=RecoveryStateMachine.reduce(RecoverySnapshot(),ConnectionEvent.START)
        s=RecoveryStateMachine.reduce(s,ConnectionEvent.TRANSPORT_READY)
        s=RecoveryStateMachine.reduce(s,ConnectionEvent.SESSION_READY)
        s=RecoveryStateMachine.reduce(s,ConnectionEvent.LOST,ConnectionReason.NETWORK_LOSS)
        assertEquals(RecoverySnapshot(ConnectionState.RECONNECT_WAIT,1,ConnectionReason.NETWORK_LOSS),s)
        s=RecoveryStateMachine.reduce(s,ConnectionEvent.RETRY_TIMER)
        assertEquals(ConnectionState.RECONNECTING,s.state)
        s=RecoveryStateMachine.reduce(s,ConnectionEvent.TRANSPORT_READY)
        assertEquals(ConnectionState.REVALIDATING,s.state)
        assertEquals(ConnectionState.READY,RecoveryStateMachine.reduce(s,ConnectionEvent.REVALIDATED).state)
    }
    @Test fun hardPrerequisitesAndExhaustedRetriesRequireUserAction() {
        assertEquals(ConnectionState.USER_ACTION_REQUIRED,RecoveryStateMachine.reduce(RecoverySnapshot(ConnectionState.READY),ConnectionEvent.LOST,ConnectionReason.PERMISSION_REVOKED).state)
        assertEquals(ConnectionState.USER_ACTION_REQUIRED,RecoveryStateMachine.reduce(RecoverySnapshot(ConnectionState.READY,RecoveryStateMachine.MAX_TRANSIENT_ATTEMPTS),ConnectionEvent.LOST,ConnectionReason.NETWORK_LOSS).state)
    }
    @Test fun unavailableCameraEndsProvisionalDiscoveryWithoutRetry() {
        val connecting = RecoveryStateMachine.reduce(RecoverySnapshot(), ConnectionEvent.START)
        val unavailable = RecoveryStateMachine.reduce(connecting, ConnectionEvent.LOST, ConnectionReason.CAMERA_UNAVAILABLE)
        assertEquals(ConnectionState.USER_ACTION_REQUIRED, unavailable.state)
        assertEquals(ConnectionReason.CAMERA_UNAVAILABLE, unavailable.reason)
        assertEquals(0, unavailable.attempts)
    }
}
