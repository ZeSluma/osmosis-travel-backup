package dev.konraditurbe.osmosis.connection

/**
 * Application/service-owned session truth. Platform adapters may perform effects only when the
 * returned epoch is current; Activity callbacks are observers and never become owners.
 */
data class SessionLease(
    val epoch: Long = 0,
    val recovery: RecoverySnapshot = RecoverySnapshot(),
    val userStopped: Boolean = false,
)

object CameraSessionCoordinator {
    fun begin(previous: SessionLease): SessionLease =
        SessionLease(epoch = previous.epoch + 1, recovery = RecoveryStateMachine.reduce(RecoverySnapshot(), ConnectionEvent.START))

    fun stop(current: SessionLease): SessionLease = current.copy(
        recovery = RecoveryStateMachine.reduce(current.recovery, ConnectionEvent.STOP),
        userStopped = true,
    )

    /** Ignores stale platform callbacks and never resurrects an explicit user stop. */
    fun event(current: SessionLease, callbackEpoch: Long, event: ConnectionEvent, reason: ConnectionReason? = null): SessionLease {
        if (callbackEpoch != current.epoch || current.userStopped) return current
        return current.copy(recovery = RecoveryStateMachine.reduce(current.recovery, event, reason))
    }

    fun mayRebuild(current: SessionLease): Boolean =
        !current.userStopped && current.recovery.state == ConnectionState.RECONNECTING

    fun mayUseCameraTraffic(current: SessionLease): Boolean =
        !current.userStopped && current.recovery.state == ConnectionState.READY
}