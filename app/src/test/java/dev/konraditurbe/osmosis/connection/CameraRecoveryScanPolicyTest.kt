package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CameraRecoveryScanPolicyTest {
    private val recovering=SessionLease(4,RecoverySnapshot(ConnectionState.RECONNECTING),false)

    @Test fun recoveryScansAreBounded(){
        assertEquals(1,CameraRecoveryScanPolicy.nextAttempt(0,recovering.copy(recovery=RecoverySnapshot(ConnectionState.RECONNECT_WAIT))))
        assertEquals(1,CameraRecoveryScanPolicy.nextAttempt(0,recovering))
        assertEquals(3,CameraRecoveryScanPolicy.nextAttempt(2,recovering))
        assertNull(CameraRecoveryScanPolicy.nextAttempt(3,recovering))
    }

    @Test fun userStopAndNonRecoveryStateCannotRestartScan(){
        assertNull(CameraRecoveryScanPolicy.nextAttempt(0,recovering.copy(userStopped=true)))
        assertNull(CameraRecoveryScanPolicy.nextAttempt(0,recovering.copy(recovery=RecoverySnapshot(ConnectionState.READY))))
    }
}
