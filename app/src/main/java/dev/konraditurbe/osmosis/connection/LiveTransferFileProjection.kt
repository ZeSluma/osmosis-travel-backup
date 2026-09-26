package dev.konraditurbe.osmosis.connection

/**
 * In-memory view of one service-owned camera-to-phone operation.  It is deliberately separate
 * from the durable badge: this projection is valid only while the owning writer exists and must
 * be cleared before a terminal durable reread.
 */
data class LiveTransferFileProjection(val phase: Phase, val percent: Int? = null) {
    enum class Phase { DOWNLOADING, WAITING_FOR_CAMERA, INTEGRITY_SAVED, REVIEW_REQUIRED }
}

object LiveTransferFileProjectionPolicy {
    fun downloading(bytesRead: Long, expectedBytes: Long): LiveTransferFileProjection {
        val percent = if (expectedBytes > 0L) ((bytesRead.coerceAtLeast(0L) * 100L) / expectedBytes)
            .toInt().coerceIn(0, 100) else 0
        return LiveTransferFileProjection(LiveTransferFileProjection.Phase.DOWNLOADING, percent)
    }

    fun completed(saved: Boolean): LiveTransferFileProjection = LiveTransferFileProjection(
        if (saved) LiveTransferFileProjection.Phase.INTEGRITY_SAVED
        else LiveTransferFileProjection.Phase.REVIEW_REQUIRED,
    )

    fun waitingForCamera(): LiveTransferFileProjection = LiveTransferFileProjection(
        LiveTransferFileProjection.Phase.WAITING_FOR_CAMERA,
    )

    fun update(
        current: Map<String, LiveTransferFileProjection>,
        key: String,
        projection: LiveTransferFileProjection,
    ): Map<String, LiveTransferFileProjection> = current + (key to projection)

    /** Terminal durable rereads always remove transient overlays. */
    fun clearAtTerminal(): Map<String, LiveTransferFileProjection> = emptyMap()
}
