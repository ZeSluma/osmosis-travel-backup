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

    @Test fun replacingOrReleasingGattInvalidatesEveryPriorCallbackGeneration() {
        val resources = CameraSessionResources()
        val first = resources.nextGattCallbackGeneration()
        assertTrue(resources.acceptsGattCallback(first))
        val second = resources.nextGattCallbackGeneration()
        assertFalse(resources.acceptsGattCallback(first))
        assertTrue(resources.acceptsGattCallback(second))
        resources.releaseGatt()
        assertFalse(resources.acceptsGattCallback(second))
    }
}
