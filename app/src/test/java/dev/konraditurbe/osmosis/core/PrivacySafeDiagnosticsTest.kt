package dev.konraditurbe.osmosis.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacySafeDiagnosticsTest {
    @Test fun `removes network device and credential values while retaining diagnosis`() {
        val safe = PrivacySafeDiagnostics.sanitize(
            "WiFi: requesting \"Pocket-Secret\" at 192.168.2.1 from AA:BB:CC:DD:EE:FF password=opensesame"
        )
        assertTrue(safe.startsWith("WiFi: requesting"))
        assertFalse(safe.contains("Pocket-Secret"))
        assertFalse(safe.contains("192.168.2.1"))
        assertFalse(safe.contains("AA:BB:CC:DD:EE:FF"))
        assertFalse(safe.contains("opensesame"))
    }

    @Test fun `removes media uris from diagnostics`() {
        assertEquals("preview <redacted-uri>",
            PrivacySafeDiagnostics.sanitize("preview http://192.168.2.1/media/DCIM/secret.mp4"))
    }
}
