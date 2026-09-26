package dev.konraditurbe.osmosis.connection

import java.util.concurrent.CopyOnWriteArraySet

/**
 * In-process notification only: observers re-read durable ledger state, so no Activity owns or
 * transports backup truth.  Registrations are explicit and removable to avoid retaining a UI.
 */
class BackupProjectionNotifier {
    private val observers = CopyOnWriteArraySet<() -> Unit>()

    fun observe(observer: () -> Unit): () -> Unit {
        observers += observer
        // A screen can return after a service transition while it had no observer.  The observer
        // always re-reads durable state, so this immediate invalidation is safe and prevents a
        // stale visible backup/progress projection from surviving a lifecycle transition.
        observer.invoke()
        return { observers -= observer }
    }

    fun publish() = observers.forEach { it.invoke() }
}
