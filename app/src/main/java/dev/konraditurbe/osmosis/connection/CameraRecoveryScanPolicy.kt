package dev.konraditurbe.osmosis.connection

/** Bounded BLE rediscovery after an unexpected live-session loss; explicit stop never retries. */
object CameraRecoveryScanPolicy {
    /** Covers a real Pocket reboot/advertising delay while remaining strictly bounded. */
    const val MAX_SCANS = 5
    const val RETRY_DELAY_MS = 1_500L

    fun nextAttempt(currentAttempts:Int, session:SessionLease):Int? = when {
        session.userStopped || session.recovery.state !in setOf(ConnectionState.RECONNECT_WAIT,ConnectionState.RECONNECTING) -> null
        currentAttempts >= MAX_SCANS -> null
        else -> currentAttempts + 1
    }

    /**
     * A live camera grid losing its selected AP cannot safely be treated as an AP-only outage.
     * A powered-off Pocket loses both its AP and BLE session; reconnecting the old Wi-Fi request
     * alone cannot wake or rediscover it. Escalate to the fenced BLE recovery path unless the
     * durable session has been explicitly stopped or cannot rebuild.
     */
    fun shouldRebuildAfterLiveApLoss(session: SessionLease, gridWasVisible: Boolean): Boolean =
        gridWasVisible && !session.userStopped &&
            session.recovery.state in setOf(ConnectionState.RECONNECT_WAIT, ConnectionState.RECONNECTING)

    /** A loss must be schedulable while waiting; RETRY_TIMER makes scanning legal afterwards. */
    fun mayScheduleAfterLoss(session: SessionLease): Boolean =
        !session.userStopped && session.recovery.state == ConnectionState.RECONNECT_WAIT

    /** Recovery scan ownership is service-epoch based, never tied to a replaceable UI field. */
    fun ownsRecoveryEpoch(rememberedEpoch: Long?, expectedEpoch: Long, session: SessionLease): Boolean =
        rememberedEpoch == expectedEpoch && session.epoch == expectedEpoch && !session.userStopped
}
