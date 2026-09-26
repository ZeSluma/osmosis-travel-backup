package dev.konraditurbe.osmosis.ui

import dev.konraditurbe.osmosis.connection.LiveTransferFileProjection
import dev.konraditurbe.osmosis.ledger.BackupDisplay

/**
 * Determines which video cells have genuinely changed backup presentation.  The service can
 * publish several observer invalidations for one durable state; repainting an unchanged grid is
 * visually disruptive and can restart thumbnail work, so it is deliberately a no-op.
 */
object BackupProjectionDiffPolicy {
    /** The initial durable projection needs a normal bind. Every later backup-only update must
     * use the dedicated payload path so it cannot restart thumbnail/meta work. */
    enum class RenderUpdate { NONE, INITIAL_FULL_BIND, BACKUP_STATE_PAYLOAD }

    fun renderUpdate(firstProjection: Boolean, changed: Set<String>): RenderUpdate = when {
        firstProjection -> RenderUpdate.INITIAL_FULL_BIND
        changed.isEmpty() -> RenderUpdate.NONE
        else -> RenderUpdate.BACKUP_STATE_PAYLOAD
    }

    fun changedKeys(
        previousDurable: Map<String, BackupDisplay>?,
        nextDurable: Map<String, BackupDisplay>,
        previousLive: Map<String, LiveTransferFileProjection>,
        nextLive: Map<String, LiveTransferFileProjection>,
    ): Set<String> {
        if (previousDurable == null) return nextDurable.keys + nextLive.keys
        return (previousDurable.keys + nextDurable.keys + previousLive.keys + nextLive.keys).filterTo(LinkedHashSet()) { key ->
            previousDurable[key] != nextDurable[key] || previousLive[key] != nextLive[key]
        }
    }
}
