package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraStartupDiscoveryPolicyTest {
    private fun connecting(epoch: Long = 9) = SessionLease(epoch, RecoverySnapshot(ConnectionState.CONNECTING))

    @Test fun knownCameraCanBeFoundAfterLauncherButDiscoveryIsBounded() {
        assertEquals(1, CameraStartupDiscoveryPolicy.nextAttempt(0, true, connecting()))
        assertEquals(8, CameraStartupDiscoveryPolicy.nextAttempt(7, true, connecting()))
        assertNull(CameraStartupDiscoveryPolicy.nextAttempt(8, true, connecting()))
    }

    @Test fun unknownStoppedOrNonConnectingSessionCannotKeepScanning() {
        assertNull(CameraStartupDiscoveryPolicy.nextAttempt(0, false, connecting()))
        assertNull(CameraStartupDiscoveryPolicy.nextAttempt(0, true, connecting().copy(userStopped = true)))
        assertNull(CameraStartupDiscoveryPolicy.nextAttempt(0, true, connecting().copy(recovery = RecoverySnapshot(ConnectionState.READY))))
    }

    @Test fun delayedScanMustStillBelongToTheLauncherEpoch() {
        assertTrue(CameraStartupDiscoveryPolicy.ownsEpoch(9, 9, connecting()))
        assertFalse(CameraStartupDiscoveryPolicy.ownsEpoch(8, 9, connecting()))
        assertFalse(CameraStartupDiscoveryPolicy.ownsEpoch(9, 10, connecting(10)))
        assertFalse(CameraStartupDiscoveryPolicy.ownsEpoch(9, 9, connecting().copy(userStopped = true)))
    }
}
