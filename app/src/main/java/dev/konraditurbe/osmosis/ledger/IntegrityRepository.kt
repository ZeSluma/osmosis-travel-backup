package dev.konraditurbe.osmosis.ledger

import androidx.room.*
import dev.konraditurbe.osmosis.integrity.*
import java.util.concurrent.Callable

/** Append-only evidence; source uncertainty never erases an already measured file receipt. */
@Entity(tableName="transfer_integrity", foreignKeys=[ForeignKey(entity=AssetRow::class,parentColumns=["id"],childColumns=["assetId"])],indices=[Index("assetId")])
data class TransferIntegrityRow(@PrimaryKey val id:String,val assetId:String,val locator:String,val localRevision:String,
    val expectedBytes:Long,val result:String,val method:String,val ownerEpoch:Long)
@Entity(tableName="source_equivalence",foreignKeys=[ForeignKey(entity=TransferIntegrityRow::class,parentColumns=["id"],childColumns=["transferId"])],indices=[Index("transferId")])
data class SourceEquivalenceRow(@PrimaryKey val id:String,val transferId:String,val sourceVersion:String,val ownerEpoch:Long,val method:String)

@Dao
interface IntegrityDao {
    @Insert fun insert(row:TransferIntegrityRow)
    @Insert fun insert(row:SourceEquivalenceRow)
    @Query("SELECT * FROM transfer_integrity WHERE id=:id") fun transfer(id:String):TransferIntegrityRow?
    @Query("SELECT * FROM transfer_integrity WHERE assetId=:asset ORDER BY id") fun transfers(asset:String):List<TransferIntegrityRow>
    @Query("SELECT * FROM source_equivalence WHERE transferId=:transfer ORDER BY id") fun sources(transfer:String):List<SourceEquivalenceRow>
}

/** Only a future measured IO adapter may supply receipts. No network/byte reader or promotion here. */
class IntegrityRepository(private val db:LedgerDatabase) {
    private fun <T> tx(block:()->T):T=db.runInTransaction(Callable(block))
    private fun asset(lease:EnumerationLease,id:String):AssetRow {
        val a=checkNotNull(db.ledger().asset(id))
        check(a.sourceId==lease.sourceId && a.lastEpoch==lease.epoch &&
            db.ledger().sourceById(lease.sourceId)?.ownerEpoch==lease.epoch) { "STALE_INTEGRITY_OWNER" }
        return a
    }
    fun recordTransfer(lease:EnumerationLease,assetId:String,local:LocalBinding,receipt:TransferReceipt):String=tx {
        val a=asset(lease,assetId)
        require(a.size==receipt.expected && receipt.expected>0)
        // Must be the already journaled replica, never an arbitrary same-name file.
        check(db.ledger().replica(assetId)?.localLocator==local.locator) { "UNJOURNALED_LOCAL_REPLICA" }
        val result=IntegrityContract.transfer(receipt,local)
        val id=key(assetId,local.locator,local.revision,receipt,result,lease.epoch)
        val row=TransferIntegrityRow(id,assetId,local.locator,local.revision,receipt.expected,result.name,"EXACT_RANGE_FLUSH_CLOSE_READBACK_V1",lease.epoch)
        val old=db.integrity().transfer(id)
        if(old==null) db.integrity().insert(row) else check(old==row)
        id
    }
    /** Explicit independently validated immutable version contract; never fill from filename/size. */
    fun recordSourceVersion(lease:EnumerationLease,transferId:String,version:String):Unit=tx {
        val t=checkNotNull(db.integrity().transfer(transferId));val a=asset(lease,t.assetId)
        require(version.isNotBlank() && !a.identityAmbiguous && a.strongVersion==version)
        require(t.result==EvidenceResult.CONFIRMED.name)
        val proof=SourceEquivalenceRow(key(transferId,version,lease.epoch),transferId,version,lease.epoch,"IMMUTABLE_SOURCE_VERSION_V1")
        val existing=db.integrity().sources(transferId).firstOrNull { it.id==proof.id }
        if(existing==null) db.integrity().insert(proof) else check(existing==proof)
    }
    /** Current binding is measured by the adapter, not inferred from historic MediaStore metadata. */
    fun evaluate(lease:EnumerationLease,assetId:String,current:LocalBinding):VerificationDimensions=tx {
        val a=asset(lease,assetId)
        val matches=db.integrity().transfers(assetId).filter {
            it.locator==current.locator && it.localRevision==current.revision && it.expectedBytes==current.bytes &&
            a.size==current.bytes
        }
        val transfer=if(matches.any{it.result==EvidenceResult.FAILED.name}) EvidenceResult.FAILED
        else if(current.readable && matches.any{it.result==EvidenceResult.CONFIRMED.name}) EvidenceResult.CONFIRMED else EvidenceResult.UNCONFIRMED
        val source=if(transfer==EvidenceResult.CONFIRMED && !a.identityAmbiguous && !a.strongVersion.isNullOrBlank() &&
            matches.any{t->db.integrity().sources(t.id).any{it.ownerEpoch==lease.epoch && it.sourceVersion==a.strongVersion}})
            EvidenceResult.CONFIRMED else EvidenceResult.UNCONFIRMED
        // Publication/readability is an additional necessary condition, never source proof.
        VerificationDimensions(transfer,source,!current.pending && db.ledger().replica(assetId)?.localLocator==current.locator)
    }
}
