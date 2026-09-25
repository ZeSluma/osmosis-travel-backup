package dev.konraditurbe.osmosis.connection

import dev.konraditurbe.osmosis.core.CameraFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraSessionResourcesTest {
    @Test fun releaseSupersedesInFlightDatalinkAndClearsOnlyCameraTransportState() {
        val resources = CameraSessionResources()
        resources.connecting = true
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
        assertFalse(resources.connecting)
        assertTrue(resources.wifiRejoins == 0)
    }

    @Test fun selectedCameraConfigurationMustFollowTransportReleaseToPublishAFreshPlan() {
        val resources = CameraSessionResources()
        resources.configureSelectedCamera("AA:BB", strictTransferSupported = true)
        resources.releaseTransport()
        assertTrue(resources.sourceAssociation == null)
        assertFalse(resources.automaticStrictTransferSupported)

        resources.configureSelectedCamera("CC:DD", strictTransferSupported = true)
        assertTrue(resources.sourceAssociation == "CC:DD")
        assertTrue(resources.automaticStrictTransferSupported)
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

    @Test fun replacingOrReleasingScannerInvalidatesEveryPriorCallbackGeneration() {
        val resources = CameraSessionResources()
        val first = resources.nextScannerCallbackGeneration()
        assertTrue(resources.acceptsScannerCallback(first))
        val second = resources.nextScannerCallbackGeneration()
        assertFalse(resources.acceptsScannerCallback(first))
        assertTrue(resources.acceptsScannerCallback(second))
        resources.releaseScanner()
        assertFalse(resources.acceptsScannerCallback(second))
    }

    @Test fun replacingOrReleasingApJoinerInvalidatesEveryPriorCallbackGeneration() {
        val resources = CameraSessionResources()
        val first = resources.nextApJoinerCallbackGeneration()
        assertTrue(resources.acceptsApJoinerCallback(first))
        val second = resources.nextApJoinerCallbackGeneration()
        assertFalse(resources.acceptsApJoinerCallback(first))
        assertTrue(resources.acceptsApJoinerCallback(second))
        resources.releaseApJoiner()
        assertFalse(resources.acceptsApJoinerCallback(second))
    }

    @Test fun terminalTrustedPageRetainsAllCurrentSessionPagesButRejectsStalePage() {
        val resources = CameraSessionResources()
        resources.ledgerSession = "current"
        resources.configureSelectedCamera("AA:BB", strictTransferSupported = true)
        assertTrue(resources.acceptsSourcePage("current", "AA:BB"))
        assertFalse(resources.acceptsSourcePage("superseded", "AA:BB"))
        assertFalse(resources.acceptsSourcePage("current", "CC:DD"))
        assertTrue(resources.recordSourcePage("current", listOf(CameraFile("first.mp4", "first.thm"))))
        assertFalse(resources.recordSourcePage("superseded", listOf(CameraFile("stale.mp4", "stale.thm"))))
        assertTrue(resources.recordSourcePage("current", listOf(CameraFile("last.mp4", "last.thm"))))

        assertTrue(resources.trustedFilesByPath.keys == setOf("first.mp4", "last.mp4"))
        resources.releaseTransport()
        assertTrue(resources.trustedFilesByPath.isEmpty())
    }
}
