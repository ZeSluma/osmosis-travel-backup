package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraRecoveryScanPolicyTest {
    private val recovering=SessionLease(4,RecoverySnapshot(ConnectionState.RECONNECTING),false)

    @Test fun recoveryScansAreBounded(){
        assertEquals(1,CameraRecoveryScanPolicy.nextAttempt(0,recovering.copy(recovery=RecoverySnapshot(ConnectionState.RECONNECT_WAIT))))
        assertEquals(1,CameraRecoveryScanPolicy.nextAttempt(0,recovering))
        assertEquals(5,CameraRecoveryScanPolicy.nextAttempt(4,recovering))
        assertNull(CameraRecoveryScanPolicy.nextAttempt(5,recovering))
    }

    @Test fun userStopAndNonRecoveryStateCannotRestartScan(){
        assertNull(CameraRecoveryScanPolicy.nextAttempt(0,recovering.copy(userStopped=true)))
        assertNull(CameraRecoveryScanPolicy.nextAttempt(0,recovering.copy(recovery=RecoverySnapshot(ConnectionState.READY))))
    }

    @Test fun liveApLossEscalatesToBleRediscoveryOnlyForARecoverableSession(){
        assertTrue(CameraRecoveryScanPolicy.shouldRebuildAfterLiveApLoss(
            recovering.copy(recovery=RecoverySnapshot(ConnectionState.RECONNECT_WAIT)), gridWasVisible=true))
        assertFalse(CameraRecoveryScanPolicy.shouldRebuildAfterLiveApLoss(recovering, gridWasVisible=false))
        assertFalse(CameraRecoveryScanPolicy.shouldRebuildAfterLiveApLoss(
            recovering.copy(userStopped=true), gridWasVisible=true))
        assertFalse(CameraRecoveryScanPolicy.shouldRebuildAfterLiveApLoss(
            recovering.copy(recovery=RecoverySnapshot(ConnectionState.READY)), gridWasVisible=true))
    }

    @Test fun reconnectWaitSchedulesTheTimerInsteadOfDeadlockingBeforeIt(){
        assertTrue(CameraRecoveryScanPolicy.mayScheduleAfterLoss(
            recovering.copy(recovery=RecoverySnapshot(ConnectionState.RECONNECT_WAIT))))
        assertFalse(CameraRecoveryScanPolicy.mayScheduleAfterLoss(recovering))
        assertFalse(CameraRecoveryScanPolicy.mayScheduleAfterLoss(recovering.copy(userStopped=true)))
    }

    @Test fun recoveryOwnershipUsesDurableEpochNotACallerLocalEpoch(){
        assertTrue(CameraRecoveryScanPolicy.ownsRecoveryEpoch(4, 4, recovering))
        assertFalse(CameraRecoveryScanPolicy.ownsRecoveryEpoch(3, 4, recovering))
        assertFalse(CameraRecoveryScanPolicy.ownsRecoveryEpoch(4, 5, recovering))
        assertFalse(CameraRecoveryScanPolicy.ownsRecoveryEpoch(4, 4, recovering.copy(userStopped=true)))
    }
}
