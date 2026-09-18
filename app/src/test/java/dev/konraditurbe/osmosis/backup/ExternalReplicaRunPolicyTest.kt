package dev.konraditurbe.osmosis.backup

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalReplicaRunPolicyTest {
    @Test fun replicaSchedulingRequiresAnAvailableSafProviderAndVerifiedPhoneWorkOnly(){
        assertFalse(ExternalReplicaRunPolicy.maySchedule(ExternalStorageAvailability.PERMISSION_REQUIRED,1))
        assertFalse(ExternalReplicaRunPolicy.maySchedule(ExternalStorageAvailability.UNAVAILABLE,1))
        assertFalse(ExternalReplicaRunPolicy.maySchedule(ExternalStorageAvailability.AVAILABLE,0))
        // There intentionally is no camera/session parameter: an offline Pocket must not block SSD catch-up.
        assertTrue(ExternalReplicaRunPolicy.maySchedule(ExternalStorageAvailability.AVAILABLE,1))
    }
}
