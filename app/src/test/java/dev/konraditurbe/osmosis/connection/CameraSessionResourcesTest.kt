package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraSessionResourcesTest {
    @Test fun releaseSupersedesInFlightDatalinkAndClearsOnlyCameraTransportState() {
        val resources = CameraSessionResources()
        resources.wifiUp = true
        resources.datalinkStarted = true
        resources.wifiRejoins = 3
        resources.resumeDownloadOnRejoin = true
        val before = resources.datalinkGeneration.get()
        resources.releaseTransport()
        assertTrue(resources.datalinkGeneration.get() > before)
        assertFalse(resources.wifiUp)
        assertFalse(resources.datalinkStarted)
        assertFalse(resources.resumeDownloadOnRejoin)
        assertTrue(resources.wifiRejoins == 0)
    }
}
