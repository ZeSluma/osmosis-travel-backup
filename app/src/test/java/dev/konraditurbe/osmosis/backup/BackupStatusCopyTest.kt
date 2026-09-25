package dev.konraditurbe.osmosis.backup

import dev.konraditurbe.osmosis.ledger.LedgerCoordinator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupStatusCopyTest {
    @Test fun unresolvedHistoricalIdentityIsExplainedWithoutAFalseCompletionClaim() {
        val text = BackupStatusCopy.plan(LedgerCoordinator.AutomaticPlanDiagnostic(
            inventoryComplete = true, completenessReason = "IDENTITY_UNRESOLVED", currentUnresolved = 0,
            historicalUnresolved = 3, downloads = 0, verifyExisting = 3, revalidate = 0, review = 0))
        assertEquals("Zuordnung von 3 früheren Dateien prüfen", text)
    }

    @Test fun summaryUsesPlainGermanButKeepsTheFailClosedMeaning() {
        val text = BackupStatusCopy.summary(BackupProductStatus(true, true, false, false, false, 3, 0),
            "Zuordnung von 3 früheren Dateien prüfen")
        assertTrue(text.contains("Kamera-Abgleich: noch offen"))
        assertTrue(text.contains("SSD-Sicherung: noch offen"))
        assertTrue(text.contains("Sicher zum Löschen: nein"))
    }

    @Test fun serviceWriterAndSourceChangeHaveActionableWording() {
        assertEquals("Übertragung gestartet (2 Dateien)", BackupStatusCopy.serviceDecision("WRITER_STARTED", 2))
        assertTrue(BackupStatusCopy.serviceDecision("SOURCE_CHANGED", 1).contains("erneut geprüft"))
    }
}
