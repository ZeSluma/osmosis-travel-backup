package dev.konraditurbe.osmosis.core

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class DiagnosticEventStoreTest {
    @Test fun `records only typed allowlisted fields`() = withStore { dir, store ->
        store.record(DiagnosticEventStore.Type.SESSION_STATE, newState = "READY", reason = "NETWORK_LOSS", retryCount = 2, bytes = 12)
        store.record(DiagnosticEventStore.Type.SESSION_STATE, newState = "DCIM/secret.mp4", reason = "password=secret")
        val text = dir.listFiles()!!.single().readText()
        assertTrue(text.contains("SESSION_STATE||READY|NETWORK_LOSS|2|12"))
        assertTrue(text.contains("SESSION_STATE||REDACTED|REDACTED||"))
        assertFalse(text.contains("secret"))
    }

    @Test fun `age and size limits bound stored diagnostic data`() {
        val dir = Files.createTempDirectory("osmosis-diagnostics").toFile()
        try {
            var now = 1_000_000L
            val store = DiagnosticEventStore.forTest(dir, { now }, maxBytes = 70, maxAgeMillis = 100)
            store.record(DiagnosticEventStore.Type.SESSION_STATE, newState = "READY")
            dir.listFiles()!!.single().setLastModified(now - 101)
            now += 1
            store.record(DiagnosticEventStore.Type.SESSION_STATE, newState = "RECONNECTING")
            assertEquals(1, dir.listFiles()!!.size)
            repeat(10) { now += 86_400_000L; store.record(DiagnosticEventStore.Type.TRANSFER_STATE, newState = "PARTIAL") }
            assertTrue(dir.listFiles()!!.sumOf { it.length() } <= 70)
        } finally { dir.deleteRecursively() }
    }

    @Test fun `export preserves only the bounded allowlisted representation`() = withStore { dir, store ->
        store.record(DiagnosticEventStore.Type.USER_ACTION_REQUIRED, reason = "CAMERA_UNAVAILABLE")
        val export = File(dir, "export.txt")
        assertEquals(export, store.exportTo(export))
        assertTrue(export.readText().contains("USER_ACTION_REQUIRED"))
    }

    private fun withStore(block: (File, DiagnosticEventStore) -> Unit) {
        val dir = Files.createTempDirectory("osmosis-diagnostics").toFile()
        try { block(dir, DiagnosticEventStore.forTest(dir, { 1_000_000L }, 1_000_000, 1_000_000)) }
        finally { dir.deleteRecursively() }
    }
}
