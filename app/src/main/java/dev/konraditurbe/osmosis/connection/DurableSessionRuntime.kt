package dev.konraditurbe.osmosis.connection

import android.content.Context
import android.content.SharedPreferences

/**
 * Small application-owned durable boundary for camera-session truth.  It deliberately stores no
 * credentials, locator, SSID, media name, or Network: those stay in the camera-only effect adapter.
 * The epoch survives a process restart so callbacks belonging to a dead owner cannot affect a
 * replacement owner.
 */
data class SourceObservation(val enumerationComplete: Boolean, val enumerationFailed: Boolean)
enum class SourceTrust { TRUSTED, INCOMPLETE_UNTRUSTED }

class DurableSessionRuntime(private val store: SessionStore, private val diagnostic: (SessionLease) -> Unit = {}) {
    private var current = store.read()
    // A process cannot retain a live writer. Its durable ledger attempt remains PARTIAL, but its
    // in-memory allocation must be released for post-revalidation recovery in the replacement host.
    private var activeTransfer: TransferLease? = null
    init { store.writeTransfer(null) }
    private val observers = linkedSetOf<(SessionLease) -> Unit>()
    // Writer release is a service coordination event, not session truth.  It lets a fresh,
    // already-trusted replacement epoch retry its durable plan after a fenced predecessor has
    // relinquished the one in-memory writer allocation.
    private val transferReleaseObservers = linkedSetOf<(TransferLease) -> Unit>()

    @Synchronized fun snapshot(): SessionLease = current
    @Synchronized fun observe(observer: (SessionLease) -> Unit): () -> Unit {
        observers += observer
        observer(current)
        return { synchronized(this) { observers -= observer } }
    }
    @Synchronized fun observeTransferRelease(observer: (TransferLease) -> Unit): () -> Unit {
        transferReleaseObservers += observer
        return { synchronized(this) { transferReleaseObservers -= observer } }
    }
    @Synchronized fun start(): SessionLease = publish(CameraSessionCoordinator.begin(current))
    @Synchronized fun stop(): SessionLease = publish(CameraSessionCoordinator.stop(current))
    @Synchronized fun callback(epoch: Long, event: ConnectionEvent, reason: ConnectionReason? = null): SessionLease =
        publish(CameraSessionCoordinator.event(current, epoch, event, reason))

    /** A reconnect only permits IO after a fresh, complete source observation. */
    @Synchronized fun revalidated(epoch: Long, source: SourceObservation): SourceTrust {
        if (epoch != current.epoch || current.userStopped) return SourceTrust.INCOMPLETE_UNTRUSTED
        if (!source.enumerationComplete || source.enumerationFailed) return SourceTrust.INCOMPLETE_UNTRUSTED
        publish(CameraSessionCoordinator.event(current, epoch, ConnectionEvent.REVALIDATED))
        return SourceTrust.TRUSTED
    }

    /**
     * The service, not an Activity instance, is the single allocation point for a transfer writer.
     * The opaque token prevents a stale finally block from releasing a replacement writer.
     */
    @Synchronized fun acquireTransfer(epoch: Long): TransferLease? {
        if (epoch != current.epoch || !CameraSessionCoordinator.mayUseCameraTraffic(current) || activeTransfer != null) return null
        val next = TransferLease(epoch, store.nextTransferGeneration())
        activeTransfer = next
        store.writeTransfer(next)
        return next
    }
    fun releaseTransfer(token: TransferLease) {
        val callbacks = synchronized(this) {
            if (activeTransfer != token) return
            activeTransfer = null
            store.writeTransfer(null)
            transferReleaseObservers.toList()
        }
        // A diagnostic/dispatcher observer cannot retain ownership or make a stale release fail.
        callbacks.forEach { runCatching { it(token) } }
    }
    @Synchronized fun activeTransfer(): TransferLease? = activeTransfer

    private fun publish(next: SessionLease): SessionLease {
        if (next == current) return current
        current = next
        store.write(next)
        // The event contains only state/reason enum names and retry count; a diagnostics failure is
        // intentionally unable to affect durable session or transfer ownership.
        runCatching { diagnostic(next) }
        observers.toList().forEach { it(next) }
        return next
    }
}

data class TransferLease(val epoch: Long, val generation: Long)

interface SessionStore {
    fun read(): SessionLease; fun write(value: SessionLease)
    fun readTransfer(): TransferLease?
    fun writeTransfer(value: TransferLease?)
    fun nextTransferGeneration(): Long
}

class PreferenceSessionStore(context: Context) : SessionStore {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("camera_session_runtime", Context.MODE_PRIVATE)
    override fun read(): SessionLease = SessionLease(
        epoch = prefs.getLong("epoch", 0L),
        recovery = RecoverySnapshot(
            state = prefs.getString("state", null)?.let { runCatching { ConnectionState.valueOf(it) }.getOrNull() } ?: ConnectionState.DISCONNECTED,
            attempts = prefs.getInt("attempts", 0),
            reason = prefs.getString("reason", null)?.let { runCatching { ConnectionReason.valueOf(it) }.getOrNull() },
        ),
        userStopped = prefs.getBoolean("user_stopped", false),
    )
    override fun write(value: SessionLease) {
        prefs.edit().putLong("epoch", value.epoch).putString("state", value.recovery.state.name)
            .putInt("attempts", value.recovery.attempts).putString("reason", value.recovery.reason?.name)
            .putBoolean("user_stopped", value.userStopped).commit()
    }
    override fun readTransfer(): TransferLease? {
        val epoch = prefs.getLong("transfer_epoch", -1L)
        val generation = prefs.getLong("transfer_generation", -1L)
        return if (epoch >= 0 && generation >= 0) TransferLease(epoch, generation) else null
    }
    override fun writeTransfer(value: TransferLease?) {
        prefs.edit().putLong("transfer_epoch", value?.epoch ?: -1L)
            .putLong("transfer_generation", value?.generation ?: -1L).commit()
    }
    override fun nextTransferGeneration(): Long = (prefs.getLong("transfer_counter", 0L) + 1L).also {
        prefs.edit().putLong("transfer_counter", it).commit()
    }
}
