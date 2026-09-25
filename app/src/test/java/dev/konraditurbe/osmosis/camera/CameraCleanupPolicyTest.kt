package dev.konraditurbe.osmosis.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CameraCleanupPolicyTest {
    @Test fun destructiveCameraProtocolIsDisabledOutsideTheDedicatedCleanupGate() {
        assertFalse(CameraCleanupPolicy.maySendDelete())
        assertEquals("CAMERA_CLEANUP_NOT_AUTHORIZED", CameraCleanupPolicy.REASON)
    }
}
