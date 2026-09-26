package dev.konraditurbe.osmosis.integrity

/**
 * Some Pocket firmwares transiently reject a newly requested original with 404/500 while the
 * previous camera-side operation is still unwinding. Retry only that observed busy shape, before
 * any destination allocation or byte write; every other response remains fail-closed.
 */
object CameraResponseRetryPolicy {
    private const val MAX_RETRIES = 3
    fun shouldRetry(status: Int, retriesAlreadyUsed: Int): Boolean =
        status in setOf(404, 500) && retriesAlreadyUsed < MAX_RETRIES
    fun delayMillis(retriesAlreadyUsed: Int): Long = when (retriesAlreadyUsed) {
        0 -> 250L
        1 -> 750L
        else -> 1_500L
    }
}
