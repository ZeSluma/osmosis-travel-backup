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
    /**
     * One short traveller-facing state, not a diagnostic dump.  A live service operation wins so
     * its adjacent progress bar has matching copy.  Otherwise surface only the next useful action.
     * This is deliberately not a cleanup permission or a claim that every historic source mapping
     * is complete.
     */
    fun summary(status: BackupProductStatus, automatic: String): String = when {
        automatic == "Backupstatus wird geladen" -> "Backupstatus wird geladen"
        automatic == incompleteInventory() -> automatic
        automatic.startsWith("Übertragung") || automatic.startsWith("Quelle hat sich") -> automatic
        // Historical ambiguity remains a global completeness blocker, but it does not make a
        // complete current camera list unsafe for a new file. The planner may therefore have just
        // transferred a new clip while older records still need review. Prefer this precise state
        // over a generic current-list warning, which would contradict the progress just shown.
        automatic.startsWith("Frühere Dateien") -> automatic
        // A durable incomplete observation is a terminal conservative outcome, not evidence that
        // an operation is still running.  Active wording here made a fully rendered grid look
        // hung indefinitely after the service had already stopped enumerating.
        !status.inventoryTrusted -> incompleteInventory()
        status.unknownRequired -> automatic
        !status.cameraSyncComplete -> "Kamera-Sicherung wird geprüft"
        !status.redundancyComplete -> "Lokal gesichert · SSD-Kopie steht noch aus"
        status.safeToClearCamera -> "Kamera und SSD sind geprüft"
        else -> "Sicherung wird geprüft"
    }

    fun awaitingTrustedInventory(): String = "warte auf eine vollständige Kameraliste"

    fun plan(diagnostic: LedgerCoordinator.AutomaticPlanDiagnostic): String = when {
        !diagnostic.inventoryComplete -> incompleteInventory()
        diagnostic.completenessReason == "IDENTITY_UNRESOLVED" || diagnostic.historicalUnresolved > 0 ->
            "Frühere Dateien brauchen Prüfung (${diagnostic.historicalUnresolved}) – neue Dateien werden weiterhin gesichert"
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
    } ?: "Backupstatus wird geladen"

    private fun plural(count: Int) = if (count == 1) "" else "en"

    /** A safe terminal state: no background work is claimed, and no camera original is at risk. */
    fun incompleteInventory(): String = "Kameraliste noch nicht vollständig – deshalb keine Übertragung"
}
