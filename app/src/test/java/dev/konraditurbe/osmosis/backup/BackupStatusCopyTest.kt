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
        assertEquals("3 ältere Dateien brauchen Aufmerksamkeit", text)
    }

    @Test fun summaryShowsOneActionableStateRatherThanTechnicalStatusDump() {
        val text = BackupStatusCopy.summary(BackupProductStatus(true, true, false, false, false, 3, 0),
            "3 ältere Dateien brauchen Aufmerksamkeit")
        assertEquals("3 ältere Dateien brauchen Aufmerksamkeit", text)
    }

    @Test fun serviceWriterAndSourceChangeHaveActionableWording() {
        assertEquals("Übertragung gestartet (2 Dateien)", BackupStatusCopy.serviceDecision("WRITER_STARTED", 2))
        assertTrue(BackupStatusCopy.serviceDecision("SOURCE_CHANGED", 1).contains("erneut geprüft"))
    }

    @Test fun summaryKeepsLiveTransferAndThenExplainsMissingSsdCopy() {
        val active = BackupProductStatus(true, false, false, false, false, 0, 0)
        assertEquals("Übertragung gestartet (1 Datei)",
            BackupStatusCopy.summary(active, "Übertragung gestartet (1 Datei)"))

        val locallyComplete = BackupProductStatus(true, false, true, false, false, 1, 0)
        assertEquals("Lokal gesichert · SSD-Kopie steht noch aus",
            BackupStatusCopy.summary(locallyComplete, "keine neue Datei zum Übertragen"))
    }

    @Test fun completeProjectionNeverRetainsAnEarlierInventoryPendingMessage() {
        val current = LedgerCoordinator.AutomaticPlanDiagnostic(
            inventoryComplete = true, completenessReason = "IDENTITY_UNRESOLVED", currentUnresolved = 0,
            historicalUnresolved = 4, downloads = 0, verifyExisting = 4, revalidate = 0, review = 0)

        val text = BackupStatusCopy.automaticStatus(current, null, "NO_WORK")

        assertEquals("4 ältere Dateien brauchen Aufmerksamkeit", text)
        assertTrue(!text.contains("Kameraliste wird noch geprüft"))
    }

    @Test fun currentCompleteInventoryWithHistoricalAmbiguityExplainsTheHistoryRatherThanLoading() {
        val diagnostic = LedgerCoordinator.AutomaticPlanDiagnostic(
            inventoryComplete = true, completenessReason = "IDENTITY_UNRESOLVED", currentUnresolved = 0,
            historicalUnresolved = 3, downloads = 0, verifyExisting = 2, revalidate = 4, review = 0)

        val text = BackupStatusCopy.plan(diagnostic)
        assertTrue(text.contains("3"))
        assertTrue(!text.contains("Kameraliste"))
    }

    @Test fun unavailableDurableProjectionIsNotMisreportedAsAnIncompleteCameraList() {
        assertEquals("Backupstatus wird geladen", BackupStatusCopy.automaticStatus(null, null, "NOT_EVALUATED"))
    }

    @Test fun visibleGridWithAnIncompleteDurableInventoryIsNotPresentedAsAnActiveOperation() {
        val incomplete = LedgerCoordinator.AutomaticPlanDiagnostic(
            inventoryComplete = false, completenessReason = "COVERAGE_UNPROVEN", currentUnresolved = 0,
            historicalUnresolved = 0, downloads = 0, verifyExisting = 0, revalidate = 0, review = 0)

        val automatic = BackupStatusCopy.automaticStatus(incomplete, null, "NO_WORK")
        assertEquals("Kameraliste noch nicht vollständig – deshalb keine Übertragung", automatic)
        assertEquals("Kameraliste noch nicht vollständig – deshalb keine Übertragung",
            BackupStatusCopy.summary(BackupProductStatus(false, true, false, false, false, 0, 0), automatic))
        assertTrue(!automatic.contains("wird geprüft"))
    }

    @Test fun durableSummaryRowsRemainConservativeAndHumanReadable() {
        assertEquals("Kamera-Sicherung wird geprüft",
            BackupStatusCopy.summary(BackupProductStatus(true, false, false, false, false, 0, 0), "keine neue Datei zum Übertragen"))
        assertEquals("Lokal gesichert · SSD-Kopie steht noch aus",
            BackupStatusCopy.summary(BackupProductStatus(true, false, true, false, false, 1, 0), "keine neue Datei zum Übertragen"))
        assertEquals("Kamera und SSD sind geprüft",
            BackupStatusCopy.summary(BackupProductStatus(true, false, true, true, true, 1, 1), "keine neue Datei zum Übertragen"))
        assertEquals("Quelle hat sich geändert – Kameraliste wird erneut geprüft",
            BackupStatusCopy.summary(BackupProductStatus(true, false, true, true, true, 1, 1),
                "Quelle hat sich geändert – Kameraliste wird erneut geprüft"))
    }
}
