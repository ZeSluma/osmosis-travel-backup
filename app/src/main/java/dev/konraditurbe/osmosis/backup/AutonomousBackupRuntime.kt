package dev.konraditurbe.osmosis.backup

import dev.konraditurbe.osmosis.connection.SourceTrust
import dev.konraditurbe.osmosis.ledger.PlanAction
import dev.konraditurbe.osmosis.ledger.PlanResult

data class BackupLease(val sessionEpoch: Long, val generation: Long)
enum class BackupPhase { IDLE, WAITING_FOR_TRUSTED_INVENTORY, CAMERA_TRANSFER, REPLICATION, USER_ACTION_REQUIRED, STOPPED }
data class BackupSnapshot(val lease: BackupLease?, val phase: BackupPhase, val reason: String? = null)

/**
 * Application/service-owned work selection. It does not transfer bytes itself: effects must present
 * the opaque [BackupLease] again before recording a result. This prevents old Activity/worker
 * callbacks from allocating or completing replacement work.
 */
class AutonomousBackupRuntime {
    private var generation = 0L
    private var current = BackupSnapshot(null, BackupPhase.IDLE)
    @Synchronized fun snapshot() = current

    @Synchronized fun plan(sessionEpoch: Long, trust: SourceTrust, plan: PlanResult?, userStopped: Boolean): Pair<BackupSnapshot, Set<String>> {
        if (userStopped) return publish(null, BackupPhase.STOPPED, "USER_STOPPED") to emptySet()
        if (trust != SourceTrust.TRUSTED || plan == null || !plan.automaticDownloadEligible)
            return publish(null, BackupPhase.WAITING_FOR_TRUSTED_INVENTORY, "INCOMPLETE_UNTRUSTED") to emptySet()
        val downloads = plan.items.filter { it.action == PlanAction.DOWNLOAD }.map { it.assetId }.toSet()
        if (downloads.isEmpty()) return publish(null, BackupPhase.REPLICATION) to emptySet()
        // Repeated observer/planner callbacks are normal.  They must not allocate a second writer
        // for the same still-current camera session; a replacement epoch deliberately gets a new
        // lease so its stale predecessor can no longer complete it.
        if (current.phase == BackupPhase.CAMERA_TRANSFER && current.lease?.sessionEpoch == sessionEpoch)
            return current to emptySet()
        val lease = BackupLease(sessionEpoch, ++generation)
        return publish(lease, BackupPhase.CAMERA_TRANSFER) to downloads
    }

    @Synchronized fun replication(sessionEpoch: Long, phoneProofs: Map<String, ReplicaStatus>, destinationAvailable: Boolean,
        userStopped: Boolean): Pair<BackupSnapshot, Set<String>> {
        if (userStopped) return publish(null, BackupPhase.STOPPED, "USER_STOPPED") to emptySet()
        if (!destinationAvailable) return publish(null, BackupPhase.USER_ACTION_REQUIRED, "SSD_UNAVAILABLE") to emptySet()
        val work = phoneProofs.filterValues { it.state == ReplicaState.VERIFIED }.keys
        if (work.isEmpty()) return publish(null, BackupPhase.IDLE) to emptySet()
        if (current.phase == BackupPhase.REPLICATION && current.lease?.sessionEpoch == sessionEpoch)
            return current to emptySet()
        val lease = BackupLease(sessionEpoch, ++generation)
        return publish(lease, BackupPhase.REPLICATION) to work
    }

    @Synchronized fun accepts(lease: BackupLease): Boolean = current.lease == lease && current.phase in setOf(BackupPhase.CAMERA_TRANSFER, BackupPhase.REPLICATION)
    @Synchronized fun stop(): BackupSnapshot = publish(null, BackupPhase.STOPPED, "USER_STOPPED")
    @Synchronized fun complete(lease: BackupLease): Boolean {
        if (!accepts(lease)) return false
        publish(null, BackupPhase.IDLE)
        return true
    }
    @Synchronized fun fail(lease: BackupLease, reason: String): Boolean {
        if (!accepts(lease)) return false
        publish(null, BackupPhase.USER_ACTION_REQUIRED, reason)
        return true
    }
    private fun publish(lease: BackupLease?, phase: BackupPhase, reason: String? = null): BackupSnapshot =
        BackupSnapshot(lease, phase, reason).also { current = it }
}

/** SAF append/resume is provider-dependent; unresolved staged objects must never trigger a second allocation. */
object ExternalReplicaRecoveryPolicy {
    fun mayAllocate(existingOperationStates:Collection<String>):Boolean =
        existingOperationStates.none { it in setOf("INTENT","COPYING","PARTIAL","CORRUPT","COMPLETE_UNFINALIZED","UNAVAILABLE") }
}

enum class StagedReplicaObservation { MISSING, PARTIAL, COMPLETE_UNFINALIZED, CORRUPT, UNAVAILABLE }
object ExternalReplicaReconciliation {
    fun classify(expected:ReplicaProof, observedBytes:Long?, observedSha256:String?):StagedReplicaObservation = when {
        observedBytes==null || observedSha256==null -> StagedReplicaObservation.UNAVAILABLE
        observedBytes==0L -> StagedReplicaObservation.PARTIAL
        observedBytes<expected.bytes -> StagedReplicaObservation.PARTIAL
        observedBytes>expected.bytes -> StagedReplicaObservation.CORRUPT
        observedSha256!=expected.sha256 -> StagedReplicaObservation.CORRUPT
        else -> StagedReplicaObservation.COMPLETE_UNFINALIZED
    }
}
