package dev.konraditurbe.osmosis.connection

/**
 * Service-layer boundary for platform transport callbacks. UI/protocol adapters report facts here;
 * this coordinator alone advances the durable session runtime. This keeps callback ordering and
 * epoch fencing out of Activity code while leaving rendering and camera protocol parsing separate.
 */
class CameraSessionEffectCoordinator(private val runtime: DurableSessionRuntime) {
    fun begin(): SessionLease = runtime.start()
    fun stop(): SessionLease = runtime.stop()
    fun stop(epoch: Long): Boolean = runtime.stopIfCurrent(epoch)
    fun transportReady(epoch: Long): SessionLease =
        runtime.callback(epoch, ConnectionEvent.TRANSPORT_READY)
    fun transportLost(epoch: Long, reason: ConnectionReason): SessionLease =
        runtime.callback(epoch, ConnectionEvent.LOST, reason)
    /** Discovery ended without a usable camera advertisement; do not leave UI in CONNECTING. */
    fun cameraUnavailable(epoch: Long): SessionLease =
        runtime.callback(epoch, ConnectionEvent.LOST, ConnectionReason.CAMERA_UNAVAILABLE)
    fun retryTimer(epoch: Long): SessionLease =
        runtime.callback(epoch, ConnectionEvent.RETRY_TIMER)
    fun revalidate(epoch: Long, enumerationComplete: Boolean, enumerationFailed: Boolean): SourceTrust =
        runtime.revalidated(epoch, SourceObservation(enumerationComplete, enumerationFailed))
    fun snapshot(): SessionLease = runtime.snapshot()
}
