package dev.konraditurbe.osmosis.backup

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean
import dev.konraditurbe.osmosis.ledger.LedgerCoordinator

/**
 * Application-owned phone→SSD runner. It intentionally has no camera/session dependency: a
 * previously verified local receipt can be copied when its approved SAF destination reappears.
 * The ledger transaction is the cross-process allocation fence; this guard only avoids duplicate
 * in-process scans and workers.
 */
class ExternalReplicaCoordinator private constructor(context: Context) {
    private val app=context.applicationContext
    private val running=AtomicBoolean(false)

    fun refreshAndReplicate() {
        if(!running.compareAndSet(false,true)) return
        Thread {
            val destination=ExternalDestinationManager(app)
            val state=destination.refreshAvailability()
            if(state!=ExternalStorageAvailability.AVAILABLE) { running.set(false); return@Thread }
            val tree=destination.selectedTree()
            val id=destination.destinationId()
            if(tree==null || id==null) { running.set(false); return@Thread }
            val ledger=LedgerCoordinator.get(app)
                ledger.reconcileExternalReplicas(id) {
                    ledger.phoneReplicaCandidates(id) { candidates ->
                    if(!ExternalReplicaRunPolicy.maySchedule(state,candidates.size)) { running.set(false); return@phoneReplicaCandidates }
                    // This callback is on the serialized ledger writer; copy work must run elsewhere.
                    Thread {
                        try {
                        candidates.forEach { candidate ->
                            ledger.replicateToExternal(candidate,id,tree) { false }
                        }
                        } finally { running.set(false) }
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
