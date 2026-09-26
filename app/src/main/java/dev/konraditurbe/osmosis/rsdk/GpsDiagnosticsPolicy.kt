package dev.konraditurbe.osmosis.rsdk

/**
 * Fixed, non-identifying GPS-service diagnostic vocabulary.  Camera names, provider names,
 * location fixes, wall-clock values and failure text are deliberately not accepted as inputs.
 */
object GpsDiagnosticsPolicy {
    fun locationWrite(ok: Boolean) = "GPS: location write=${if (ok) "OK" else "FAILED"}"
    fun duplicateStart() = "GPS: duplicate start ignored"
    fun providerSubscription(active: Boolean) = "GPS: location providers subscribed=$active"
    fun cameraConnectionFailed() = "GPS: camera connection failed"
    fun localWallClockMode() = "GPS: local wall-clock mode active"
}
