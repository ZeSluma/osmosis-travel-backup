package dev.konraditurbe.osmosis.core

/** Verbose diagnostics are explicit, temporary and never recover automatically after a restart. */
object VerboseDiagnosticsPolicy {
    const val MAX_DURATION_MILLIS = 30L * 60L * 1000L
    const val MAX_BYTES = 10L * 1024L * 1024L
    fun mayAppend(startedAtMillis: Long, nowMillis: Long, currentBytes: Long, nextBytes: Long): Boolean =
        nowMillis - startedAtMillis in 0..MAX_DURATION_MILLIS &&
            currentBytes in 0..MAX_BYTES && nextBytes >= 0 && currentBytes <= MAX_BYTES - nextBytes
}
