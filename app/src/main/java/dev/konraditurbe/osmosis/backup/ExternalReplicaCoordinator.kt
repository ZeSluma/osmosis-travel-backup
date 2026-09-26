package dev.konraditurbe.osmosis.backup

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean
import dev.konraditurbe.osmosis.connection.CameraConnectionService
import dev.konraditurbe.osmosis.ledger.LedgerCoordinator
import dev.konraditurbe.osmosis.core.DiagnosticEventStore

/**
 * Application-owned phone→SSD runner. It intentionally has no camera/session dependency: a
 * previously verified local receipt can be copied when its approved SAF destination reappears.
 * The ledger transaction is the cross-process allocation fence; this guard only avoids duplicate
 * in-process scans and workers.
 */
class ExternalReplicaCoordinator private constructor(context: Context) {
    data class Progress(val completedFiles: Int, val totalFiles: Int) {
        val percent: Int = if (totalFiles > 0) ((completedFiles * 100) / totalFiles).coerceIn(0, 100) else 0
    }
    private val app=context.applicationContext
    private val running=AtomicBoolean(false)
    // One terminal publication must refresh the screen from durable rows, but that refresh must
    // not immediately schedule the same failed provider operation again.
    private val skipOneObserverRefresh=AtomicBoolean(false)
    /** Transient UI liveness only; final truth is always reread from replica receipts. */
    @Volatile var progress: Progress? = null
        private set

    fun refreshAndReplicate() {
        if (!ExternalReplicaRefreshPolicy.shouldStart(skipOneObserverRefresh.get())) {
            skipOneObserverRefresh.compareAndSet(true, false)
            return
        }
        if(!running.compareAndSet(false,true)) return
        Thread {
            val destination=ExternalDestinationManager(app)
            val state=destination.refreshAvailability()
            DiagnosticEventStore.open(app).record(DiagnosticEventStore.Type.STORAGE_STATE, newState = state.name)
            if(state!=ExternalStorageAvailability.AVAILABLE) { running.set(false); return@Thread }
            val tree=destination.selectedTree()
            val id=destination.destinationId()
            if(tree==null || id==null) { running.set(false); return@Thread }
            val ledger=LedgerCoordinator.get(app)
                ledger.reconcileExternalReplicas(id) {
                    ledger.phoneReplicaCandidates(id) { candidates ->
                    if(!ExternalReplicaRunPolicy.maySchedule(state,candidates.size)) {
                        DiagnosticEventStore.open(app).record(DiagnosticEventStore.Type.STORAGE_STATE, newState = "NO_REPLICA_WORK")
                        running.set(false); return@phoneReplicaCandidates
                    }
                    // This callback is on the serialized ledger writer; copy work must run elsewhere.
                    Thread {
                        progress = Progress(0, candidates.size)
                        CameraConnectionService.backupProjectionNotifier(app).publish()
                        try {
                        candidates.forEachIndexed { index, candidate ->
                            ledger.replicateToExternal(candidate,id,tree) { false }
                            progress = Progress(index + 1, candidates.size)
                            CameraConnectionService.backupProjectionNotifier(app).publish()
                        }
                        DiagnosticEventStore.open(app).record(DiagnosticEventStore.Type.STORAGE_STATE, newState = "REPLICA_RUN_COMPLETE")
                        } finally {
                            running.set(false)
                            progress = null
                            skipOneObserverRefresh.set(true)
                            // The replica ledger rows changed off the Activity thread. Notify the
                            // observer only after a real copy run, so the durable phone/SSD
                            // projection is reread without requiring an app restart. A no-work
                            // availability probe deliberately does not publish: it would create a
                            // self-refresh loop from MainActivity's observer.
                            CameraConnectionService.backupProjectionNotifier(app).publish()
                        }
                    }.apply { name="osmosis-external-replica-copy" }.start()
                }
            }
        }.apply { name="osmosis-external-replica" }.start()
    }

    companion object {
        @Volatile private var instance:ExternalReplicaCoordinator?=null
        fun get(context:Context):ExternalReplicaCoordinator=instance ?: synchronized(this) {
            instance ?: ExternalReplicaCoordinator(context).also { instance=it }
        }
    }
}
