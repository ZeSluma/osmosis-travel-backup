package dev.konraditurbe.osmosis.backup

import dev.konraditurbe.osmosis.ledger.*
import java.time.Instant
import java.util.UUID
import java.util.concurrent.Callable

/** Durable SSD destination and replica evidence, owned by the ledger transaction boundary. */
class ReplicaEvidenceRepository(private val db: LedgerDatabase) {
    private fun <T> tx(block: () -> T): T = db.runInTransaction(Callable(block))

    fun registerDestination(id: String, treeUri: String, available: Boolean, now: Instant = Instant.now()) = tx {
        require(id.matches(Regex("[A-Za-z0-9._-]{1,120}")))
        require(treeUri.startsWith("content://") && !treeUri.contains('?') && !treeUri.contains('#'))
        val next = StorageDestinationRow(id, treeUri, StorageDomain.EXTERNAL_SAF.name,
            if (available) "AVAILABLE" else "UNAVAILABLE", now.toString(), null)
        val old = db.ledger().storageDestination(id)
        if (old == null) db.ledger().storageDestination(next)
        else require(old.treeUri == treeUri) { "DESTINATION_IDENTITY_CHANGED_REVIEW_REQUIRED" }
    }

    fun availability(id: String, available: Boolean, reason: String? = null, now: Instant = Instant.now()) = tx {
        val old = checkNotNull(db.ledger().storageDestination(id))
        db.ledger().storageDestination(old.copy(state = if (available) "AVAILABLE" else "UNAVAILABLE",
            lastValidatedAt = now.toString(), failure = if (available) null else reason ?: "UNAVAILABLE"))
    }

    fun recordVerified(lease: EnumerationLease, assetId: String, destinationId: String, locator: String, proof: ReplicaProof) = tx {
        check(db.ledger().sourceById(lease.sourceId)?.ownerEpoch == lease.epoch) { "STALE_REPLICA_OWNER" }
        val asset = checkNotNull(db.ledger().asset(assetId))
        require(asset.sourceId == lease.sourceId && asset.lastEpoch == lease.epoch)
        val destination = checkNotNull(db.ledger().storageDestination(destinationId))
        require(destination.state == "AVAILABLE" && proof.bytes > 0 && proof.sha256.matches(Regex("[0-9a-f]{64}")))
        require(locator.startsWith("content://") && !locator.contains('?') && !locator.contains('#'))
        val row = ReplicaIntegrityRow(key(assetId, destinationId, locator, proof.bytes, proof.sha256, lease.epoch),
            assetId, destinationId, locator, proof.bytes, proof.sha256, ReplicaState.VERIFIED.name, lease.epoch)
        if (db.ledger().replicaProofs(assetId, destinationId).none { it.id == row.id }) db.ledger().replicaProof(row)
    }

    fun beginOperation(lease: EnumerationLease, assetId: String, destinationId: String, phoneProof: ReplicaProof): String = tx {
        check(db.ledger().sourceById(lease.sourceId)?.ownerEpoch == lease.epoch) { "STALE_REPLICA_OWNER" }
        require(checkNotNull(db.ledger().asset(assetId)).lastEpoch == lease.epoch)
        require(checkNotNull(db.ledger().storageDestination(destinationId)).state == "AVAILABLE")
        val id=UUID.randomUUID().toString()
        db.ledger().replicaOperation(ReplicaOperationRow(id,assetId,destinationId,null,phoneProof.bytes,phoneProof.sha256,"INTENT",0,lease.epoch))
        id
    }
    fun attachOperation(lease: EnumerationLease, id: String, locator: String) = updateOperation(lease,id) {
        require(it.state=="INTENT" && locator.startsWith("content://")); it.copy(locator=locator,state="COPYING")
    }
    fun checkpointOperation(lease: EnumerationLease, id: String, bytes: Long) = updateOperation(lease,id) {
        require(it.state=="COPYING" && bytes>=it.checkpoint && bytes<=it.expectedBytes);it.copy(checkpoint=bytes)
    }
    fun finishOperation(lease: EnumerationLease, id: String, verified: Boolean) = updateOperation(lease,id) {
        it.copy(state=if(verified)"VERIFIED" else "PARTIAL")
    }
    fun allocationFailed(lease:EnumerationLease,id:String)=updateOperation(lease,id) {
        require(it.state=="INTENT");it.copy(state="ALLOCATION_FAILED")
    }
    /** New post-revalidation owner may classify an old staged object but cannot promote it. */
    fun reconcileOperation(lease:EnumerationLease,id:String,observation:StagedReplicaObservation)=tx {
        val current=checkNotNull(db.ledger().replicaOperation(id));val asset=checkNotNull(db.ledger().asset(current.assetId))
        check(asset.sourceId==lease.sourceId && asset.lastEpoch==lease.epoch && db.ledger().sourceById(lease.sourceId)?.ownerEpoch==lease.epoch){"STALE_RECONCILIATION_OWNER"}
        val next=when(observation) {
            StagedReplicaObservation.MISSING -> current.copy(state="MISSING",checkpoint=0,ownerEpoch=lease.epoch)
            StagedReplicaObservation.PARTIAL -> current.copy(state="PARTIAL",ownerEpoch=lease.epoch)
            StagedReplicaObservation.COMPLETE_UNFINALIZED -> current.copy(state="COMPLETE_UNFINALIZED",ownerEpoch=lease.epoch)
            StagedReplicaObservation.CORRUPT -> current.copy(state="CORRUPT",ownerEpoch=lease.epoch)
            StagedReplicaObservation.UNAVAILABLE -> current.copy(state="UNAVAILABLE",ownerEpoch=lease.epoch)
        }
        db.ledger().updateReplicaOperation(next)
    }
    private fun updateOperation(lease: EnumerationLease,id:String, update:(ReplicaOperationRow)->ReplicaOperationRow)=tx {
        val current=checkNotNull(db.ledger().replicaOperation(id))
        check(current.ownerEpoch==lease.epoch && db.ledger().sourceById(lease.sourceId)?.ownerEpoch==lease.epoch){"STALE_REPLICA_OPERATION"}
        db.ledger().updateReplicaOperation(update(current))
    }
}
