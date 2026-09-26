package dev.konraditurbe.osmosis.rsdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GpsDiagnosticsPolicyTest {
    @Test fun serviceCopyHasNoInputChannelForCameraLocationOrFailureText() {
        val lines = listOf(
            GpsDiagnosticsPolicy.locationWrite(true),
            GpsDiagnosticsPolicy.locationWrite(false),
            GpsDiagnosticsPolicy.duplicateStart(),
            GpsDiagnosticsPolicy.providerSubscription(true),
            GpsDiagnosticsPolicy.cameraConnectionFailed(),
            GpsDiagnosticsPolicy.localWallClockMode(),
        )
        assertEquals(setOf(
            "GPS: location write=OK", "GPS: location write=FAILED", "GPS: duplicate start ignored",
            "GPS: location providers subscribed=true", "GPS: camera connection failed",
            "GPS: local wall-clock mode active",
        ), lines.toSet())
        val text = lines.joinToString("\\n")
        assertFalse(text.contains("Slumas"))
        assertFalse(text.contains("192.168."))
        assertFalse(text.contains("UTC+"))
    }
}
