package dev.konraditurbe.osmosis.backup

import dev.konraditurbe.osmosis.ledger.LedgerCoordinator

/**
 * Human-facing, privacy-safe copy for the persistent backup projection.
 *
 * The ledger keeps exact machine reasons; this boundary deliberately converts only safe counts and
 * state into wording a traveller can act on.  It must never imply source equivalence, redundancy or
 * cleanup eligibility that the projection has not established.
 */
object BackupStatusCopy {
    fun summary(status: BackupProductStatus, automatic: String): String = listOf(
        "Kamera-Abgleich: ${if (status.cameraSyncComplete) "abgeschlossen" else "noch offen"}",
        "SSD-Sicherung: ${if (status.redundancyComplete) "abgeschlossen" else "noch offen"}",
        "Sicher zum Löschen: ${if (status.safeToClearCamera) "ja (nur Hinweis)" else "nein"}",
        "Automatisch: $automatic"
    ).joinToString(" · ")

    fun awaitingTrustedInventory(): String = "warte auf eine vollständige Kameraliste"

    fun plan(diagnostic: LedgerCoordinator.AutomaticPlanDiagnostic): String = when {
        !diagnostic.inventoryComplete -> "Kameraliste wird noch geprüft"
        diagnostic.completenessReason == "IDENTITY_UNRESOLVED" || diagnostic.historicalUnresolved > 0 ->
            "Zuordnung von ${diagnostic.historicalUnresolved} früheren Datei${plural(diagnostic.historicalUnresolved)} prüfen"
        diagnostic.currentUnresolved > 0 -> "${diagnostic.currentUnresolved} Datei${plural(diagnostic.currentUnresolved)} auf der Kamera prüfen"
        diagnostic.downloads > 0 -> "${diagnostic.downloads} neue Datei${plural(diagnostic.downloads)} werden vorbereitet"
        diagnostic.verifyExisting > 0 -> "${diagnostic.verifyExisting} lokale Datei${plural(diagnostic.verifyExisting)} werden geprüft"
        diagnostic.revalidate > 0 -> "${diagnostic.revalidate} Datei${plural(diagnostic.revalidate)} werden erneut abgeglichen"
        diagnostic.review > 0 -> "${diagnostic.review} Datei${plural(diagnostic.review)} brauchen eine Zuordnungsprüfung"
        else -> "keine neue Datei zum Übertragen"
    }

    fun serviceDecision(decision: String, count: Int): String = when {
        decision.startsWith("WRITER_STARTED") -> "Übertragung gestartet ($count Datei${plural(count)})"
        decision.startsWith("SOURCE_CHANGED") -> "Quelle hat sich geändert – Kameraliste wird erneut geprüft"
        decision.startsWith("NO_WORK") -> "keine neue Datei zum Übertragen"
        else -> "Übertragung wird sicher vorbereitet ($count Datei${plural(count)})"
    }

    /**
     * Render automatic work from one current durable projection.  The caller deliberately passes
     * no previous UI text: a former incomplete enumeration may never survive a newer complete
     * plan as stale copy.
     */
    fun automaticStatus(diagnostic: LedgerCoordinator.AutomaticPlanDiagnostic?, serviceProgress: String?,
        serviceDecision: String): String = serviceProgress ?: diagnostic?.let {
        if (it.downloads > 0) serviceDecision(serviceDecision, it.downloads) else plan(it)
    } ?: "Kameraliste wird geprüft"

    private fun plural(count: Int) = if (count == 1) "" else "en"
}
