package dev.konraditurbe.osmosis.ledger

import androidx.room.*
import java.util.concurrent.Callable

@Entity(tableName="transfer_attempts",foreignKeys=[ForeignKey(entity=AssetRow::class,parentColumns=["id"],childColumns=["assetId"])],indices=[Index("assetId")])
data class TransferAttemptRow(@PrimaryKey val id:String,val assetId:String,val ownerEpoch:Long,val expectedBytes:Long,
    val state:String,val locator:String?,val checkpoint:Long,val integrityId:String?)
@Dao
interface AttemptDao {
    @Insert fun insert(row:TransferAttemptRow)
    @Update fun update(row:TransferAttemptRow)
    @Query("SELECT * FROM transfer_attempts WHERE id=:id") fun get(id:String):TransferAttemptRow?
    @Query("SELECT * FROM transfer_attempts WHERE assetId=:asset ORDER BY id") fun forAsset(asset:String):List<TransferAttemptRow>
}

/** Journal only. IO adapters must prove exclusive pending ownership; never adopt by name. */
class AttemptRepository(private val db:LedgerDatabase) {
    private fun <T> tx(block:()->T):T=db.runInTransaction(Callable(block))
    private fun current(lease:EnumerationLease,assetId:String) {
        val a=checkNotNull(db.ledger().asset(assetId))
        check(a.sourceId==lease.sourceId && a.lastEpoch==lease.epoch && db.ledger().sourceById(lease.sourceId)?.ownerEpoch==lease.epoch)
    }
    private fun owned(lease:EnumerationLease,id:String):TransferAttemptRow {
        val row=checkNotNull(db.attempts().get(id));current(lease,row.assetId)
        check(row.ownerEpoch==lease.epoch);return row
    }
    fun begin(lease:EnumerationLease,assetId:String):TransferAttemptRow=tx {
        current(lease,assetId)
        val id=key("transfer-attempt",assetId,lease.epoch)
        db.attempts().get(id)?.let{return@tx it}
        val a=db.ledger().asset(assetId)!!;val p=db.ledger().replica(assetId)!!
        require(a.size!=null && a.size>0 && a.classification=="KNOWN_REQUIRED")
        check(p.localLocator==null && p.localPresence=="ABSENT" && p.committedLength==0L)
        // Unreconciled prior intent may own an orphan; never silently create another copy.
        check(db.attempts().forAsset(assetId).isEmpty()) { "PRIOR_ATTEMPT_REQUIRES_RECOVERY" }
        val row=TransferAttemptRow(id,assetId,lease.epoch,a.size,"INTENT",null,0,null)
        db.attempts().insert(row);row
    }
    fun reserveAllocation(lease:EnumerationLease,id:String)=tx {
        val row=owned(lease,id);check(row.state=="INTENT")
        db.attempts().update(row.copy(state="ALLOCATING"))
    }
    fun attach(lease:EnumerationLease,id:String,ownedPendingLocator:String)=tx {
        val row=owned(lease,id);require(ownedPendingLocator.isNotBlank())
        if(row.state=="WRITING" && row.locator==ownedPendingLocator)return@tx
        check(row.state=="ALLOCATING" && row.locator==null)
        check(db.ledger().replica(row.assetId)!!.localLocator==null)
        db.attempts().update(row.copy(state="WRITING",locator=ownedPendingLocator))
        val p=db.ledger().replica(row.assetId)!!
        db.ledger().replica(p.copy(localLocator=ownedPendingLocator,state="PARTIAL",localPresence="NOT_SCANNED"))
    }
    /** Invoke only after a successful destination sync. Ledger failure leaves trailing bytes untrusted. */
    fun checkpoint(lease:EnumerationLease,id:String,syncedLength:Long)=tx {
        val row=owned(lease,id);check(row.state in setOf("WRITING","PARTIAL"))
        require(syncedLength>=row.checkpoint && syncedLength<=row.expectedBytes)
        db.attempts().update(row.copy(state="PARTIAL",checkpoint=syncedLength))
        val p=db.ledger().replica(row.assetId)!!
        db.ledger().replica(p.copy(committedLength=syncedLength,state="PARTIAL"))
    }
    fun preparePublication(lease:EnumerationLease,id:String,integrityId:String)=tx {
        val row=owned(lease,id);check(row.state in setOf("WRITING","PARTIAL","PUBLISH_PENDING"))
        val evidence=checkNotNull(db.integrity().transfer(integrityId))
        require(evidence.assetId==row.assetId && evidence.locator==row.locator && evidence.ownerEpoch==lease.epoch &&
            evidence.expectedBytes==row.expectedBytes && evidence.result=="CONFIRMED" && row.checkpoint==row.expectedBytes)
        db.attempts().update(row.copy(state="PUBLISH_PENDING",integrityId=integrityId))
        // Publication alone never establishes source equivalence or LOCAL_VERIFIED.
        val p=db.ledger().replica(row.assetId)!!
        db.ledger().replica(p.copy(state="TRANSFERRED_UNVERIFIED"))
    }
    fun observePublished(lease:EnumerationLease,id:String,local:dev.konraditurbe.osmosis.integrity.LocalBinding)=tx {
        val row=owned(lease,id);check(row.state in setOf("PUBLISH_PENDING","PUBLISHED"))
        val proof=checkNotNull(db.integrity().transfer(checkNotNull(row.integrityId)))
        require(local.locator==row.locator && local.revision==proof.localRevision && local.bytes==row.expectedBytes && local.readable && !local.pending)
        db.attempts().update(row.copy(state="PUBLISHED"))
    }
}
