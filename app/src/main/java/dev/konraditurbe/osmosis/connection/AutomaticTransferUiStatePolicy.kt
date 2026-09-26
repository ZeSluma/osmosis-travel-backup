package dev.konraditurbe.osmosis.connection

/**
 * Maps the service's intentionally small progress projection to presentation states.  This is
 * advisory UI only: completed or verified asset badges still come exclusively from the ledger.
 */
object AutomaticTransferUiStatePolicy {
    enum class Phase { PREPARING, WAITING_FOR_WRITER, TRANSFERRING, FINISHED }
    data class State(
        val phase: Phase,
        val percent: Int? = null,
        val fileCount: Int? = null,
        /** Terminal acknowledgement only; null means keep observing the current operation. */
        val dismissAfterMs: Long? = null,
    )

    private val progress = Regex("^transfer=(\\d{1,3})% \\((\\d+)/(\\d+)\\)$")

    fun project(serviceProgress: String?, serviceDecision: String): State? {
        return when {
            // A newer terminal service decision always wins over a delayed progress callback.
            // Otherwise a superseded writer could repaint an already-failed/reviewed replacement
            // as an active transfer.
            serviceDecision == "TRANSFER_REVIEW_REQUIRED" || serviceDecision == "SOURCE_CHANGED" ||
                serviceDecision in setOf("NOT_EVALUATED", "NO_LEDGER_SESSION", "NO_CAMERA_NETWORK",
                    "USER_STOPPED", "STRICT_TRANSFER_UNSUPPORTED", "SESSION_NOT_READY",
                    "STALE_OR_UNTRUSTED", "PLAN_NOT_ELIGIBLE", "WRITER_ALREADY_ACTIVE", "NO_WORK") -> null
            serviceDecision == "WRITER_COMPLETE" -> State(Phase.FINISHED, 100, dismissAfterMs = 3_000L)
            serviceDecision == "PLAN_LOOKUP" -> State(Phase.PREPARING)
            serviceDecision == "WRITER_WAIT" -> State(Phase.WAITING_FOR_WRITER)
            serviceDecision.startsWith("WRITER_STARTED") -> progress.matchEntire(serviceProgress.orEmpty())?.let { match ->
                State(Phase.TRANSFERRING, match.groupValues[1].toInt().coerceIn(0, 100),
                    match.groupValues[3].toIntOrNull()?.coerceAtLeast(0))
            }
            else -> null
        }
    }
}
