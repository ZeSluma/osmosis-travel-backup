package dev.konraditurbe.osmosis.rsdk

/** Admission policy for optional GPS recording sync. */
object GpsModePolicy {
    /** A fresh process starts in normal backup mode; GPS needs new user intent. */
    fun initialModeAfterLaunch(): Boolean = false

    /** Location telemetry is only allowed after a current explicit camera selection. */
    fun mayStartGps(explicitMode: Boolean, userSelectedCamera: Boolean): Boolean =
        explicitMode && userSelectedCamera

    /** Automatic known-camera discovery is always the backup path unless GPS owns the camera. */
    fun mayAutoStartBackup(gpsServiceOwnsCamera: Boolean): Boolean = !gpsServiceOwnsCamera
}
