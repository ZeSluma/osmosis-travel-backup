package dev.konraditurbe.osmosis.connection

/**
 * Bounded discovery for a known camera that powers on shortly after a fresh launcher start.
 * This is deliberately separate from recovery: no prior live transport is required and an
 * explicit user stop always wins.  A finite budget avoids hidden background scanning.
 */
object CameraStartupDiscoveryPolicy {
    const val MAX_SCANS = 8
    const val RETRY_DELAY_MS = 1_500L

    fun nextAttempt(currentAttempts: Int, hasSavedCamera: Boolean, session: SessionLease): Int? = when {
        !hasSavedCamera || session.userStopped || session.recovery.state != ConnectionState.CONNECTING -> null
        currentAttempts >= MAX_SCANS -> null
        else -> currentAttempts + 1
    }

    fun ownsEpoch(rememberedEpoch: Long?, expectedEpoch: Long, session: SessionLease): Boolean =
        rememberedEpoch == expectedEpoch && session.epoch == expectedEpoch &&
            !session.userStopped && session.recovery.state == ConnectionState.CONNECTING
}
