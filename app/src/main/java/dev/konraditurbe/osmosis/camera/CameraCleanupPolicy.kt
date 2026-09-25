package dev.konraditurbe.osmosis.camera

/**
 * Project-level gate for destructive camera operations.
 *
 * Camera cleanup has its own authorization and revalidation workflow.  Until that workflow is
 * explicitly enabled, UI affordances and stale callbacks must fail closed instead of sending a
 * best-effort protocol delete merely because a manifest handle is present.
 */
object CameraCleanupPolicy {
    const val REASON = "CAMERA_CLEANUP_NOT_AUTHORIZED"

    fun maySendDelete(): Boolean = false
}
