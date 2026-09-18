package dev.konraditurbe.osmosis.backup

import org.junit.Assert.*
import org.junit.Test

class ExternalStoragePolicyTest {
    @Test fun permissionUnknownAndLowSpaceAllBlockAllocation(){
        assertEquals(ExternalStorageAvailability.PERMISSION_REQUIRED,ExternalStoragePolicy.assess(false,100,10))
        assertEquals(ExternalStorageAvailability.UNAVAILABLE,ExternalStoragePolicy.assess(true,null,10))
        assertEquals(ExternalStorageAvailability.INSUFFICIENT_SPACE,ExternalStoragePolicy.assess(true,9,10))
    }
    @Test fun onlyKnownSufficientSpaceIsAvailable(){
        assertEquals(ExternalStorageAvailability.AVAILABLE,ExternalStoragePolicy.assess(true,10,10))
    }
}
