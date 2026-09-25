package dev.konraditurbe.osmosis.rsdk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GpsModePolicyTest {
    @Test fun processLaunchCannotRestoreGpsAsTheAutomaticBackupMode() {
        assertFalse(GpsModePolicy.initialModeAfterLaunch())
    }

    @Test fun gpsRequiresExplicitCurrentSelection() {
        assertFalse(GpsModePolicy.mayStartGps(explicitMode = false, userSelectedCamera = true))
        assertFalse(GpsModePolicy.mayStartGps(explicitMode = true, userSelectedCamera = false))
        assertTrue(GpsModePolicy.mayStartGps(explicitMode = true, userSelectedCamera = true))
    }

    @Test fun automaticDiscoveryIsBackupUnlessGpsActuallyOwnsTheCamera() {
        assertTrue(GpsModePolicy.mayAutoStartBackup(gpsServiceOwnsCamera = false))
        assertFalse(GpsModePolicy.mayAutoStartBackup(gpsServiceOwnsCamera = true))
    }
}
