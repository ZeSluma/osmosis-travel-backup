package dev.konraditurbe.osmosis.connection

/** Bounded BLE rediscovery after an unexpected live-session loss; explicit stop never retries. */
object CameraRecoveryScanPolicy {
    const val MAX_SCANS = 3
    const val RETRY_DELAY_MS = 1_500L

    fun nextAttempt(currentAttempts:Int, session:SessionLease):Int? = when {
        session.userStopped || session.recovery.state !in setOf(ConnectionState.RECONNECT_WAIT,ConnectionState.RECONNECTING) -> null
        currentAttempts >= MAX_SCANS -> null
        else -> currentAttempts + 1
    }
}
