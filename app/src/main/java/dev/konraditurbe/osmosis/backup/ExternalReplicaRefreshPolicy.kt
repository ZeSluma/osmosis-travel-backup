package dev.konraditurbe.osmosis.backup

/** Pure counterpart to the coordinator's one-shot terminal observer suppression. */
object ExternalReplicaRefreshPolicy {
    fun shouldStart(skipOneTerminalRefresh: Boolean): Boolean = !skipOneTerminalRefresh
}
