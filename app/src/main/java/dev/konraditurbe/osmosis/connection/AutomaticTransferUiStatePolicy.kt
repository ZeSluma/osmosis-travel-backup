package dev.konraditurbe.osmosis.connection

/**
 * Maps the service's intentionally small progress projection to presentation states.  This is
 * advisory UI only: completed or verified asset badges still come exclusively from the ledger.
 */
object AutomaticTransferUiStatePolicy {
    enum class Phase { PREPARING, WAITING_FOR_WRITER, TRANSFERRING, FINISHED, REVIEW_REQUIRED }
    data class State(val phase: Phase, val percent: Int? = null, val fileCount: Int? = null)

    private val progress = Regex("^transfer=(\\d{1,3})% \\((\\d+)/(\\d+)\\)$")

    fun project(serviceProgress: String?, serviceDecision: String): State? {
        progress.matchEntire(serviceProgress.orEmpty())?.let { match ->
            return State(Phase.TRANSFERRING, match.groupValues[1].toInt().coerceIn(0, 100),
                match.groupValues[3].toIntOrNull()?.coerceAtLeast(0))
        }
        return when {
            serviceDecision == "PLAN_LOOKUP" -> State(Phase.PREPARING)
            serviceDecision == "WRITER_WAIT" -> State(Phase.WAITING_FOR_WRITER)
            serviceDecision == "WRITER_COMPLETE" -> State(Phase.FINISHED, 100)
            serviceDecision == "TRANSFER_REVIEW_REQUIRED" || serviceDecision == "SOURCE_CHANGED" ->
                State(Phase.REVIEW_REQUIRED)
            else -> null
        }
    }
}
