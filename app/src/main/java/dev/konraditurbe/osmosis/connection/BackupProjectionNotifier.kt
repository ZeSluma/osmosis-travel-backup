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
        return { observers -= observer }
    }

    fun publish() = observers.forEach { it.invoke() }
}
