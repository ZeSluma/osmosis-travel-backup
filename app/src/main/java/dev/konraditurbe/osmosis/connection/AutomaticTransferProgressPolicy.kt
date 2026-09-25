package dev.konraditurbe.osmosis.connection

/**
 * Privacy-safe projection for an active service-owned transfer.  The durable ledger remains the
 * source of truth for partial receipts; this only gives an attached observer honest liveness.
 */
object AutomaticTransferProgressPolicy {
    data class Projection(val text: String, val percent: Long)

    fun project(totalFiles: Int, totalBytes: Long, completedFiles: Int, overallDone: Long): Projection {
        val safeFiles = totalFiles.coerceAtLeast(0)
        val safeDone = overallDone.coerceAtLeast(0L)
        val percent = if (totalBytes > 0L) ((safeDone * 100L) / totalBytes).coerceIn(0L, 100L) else 0L
        return Projection("transfer=$percent% ($completedFiles/$safeFiles)", percent)
    }

    /** Avoid projecting every stream buffer while still making a live transfer visibly advance. */
    fun shouldPublish(previousPercent: Long, nextPercent: Long): Boolean = previousPercent != nextPercent
}
