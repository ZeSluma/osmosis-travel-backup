package dev.konraditurbe.osmosis.integrity

import dev.konraditurbe.osmosis.ledger.*
import java.io.InputStream
import java.io.OutputStream

interface TransferResponse:AutoCloseable {
    val metadata:ResponseMetadata
    /** Null unless the adapter independently establishes immutable version semantics for this response. */
    val sourceRevision:SourceRevision? get()=null
    fun input():InputStream
}
interface OwnedPendingDestination {
    val locator:String
    fun output():OutputStream
    /** Must sync the actual file before journal checkpoint; flush alone is insufficient. */
    fun sync()
    fun inspect():LocalBinding
    fun input():InputStream
    fun publish()
}

/** Single explicit full-original operation; no automatic scheduling, deletion, or verified promotion. */
class SingleAssetTransfer(private val db:LedgerDatabase) {
    enum class Result { TRANSFERRED_UNVERIFIED, PARTIAL_OR_REVIEW_REQUIRED }
    fun start(lease:EnumerationLease,assetId:String,source:()->TransferResponse,
        createOwnedPending:(String)->OwnedPendingDestination,cancelled:()->Boolean={false},progress:(Long)->Unit={}):Result {
        val journal=AttemptRepository(db)
        return try {
            val intent=journal.begin(lease,assetId)
            // Previous allocation may have created an orphan. Never allocate again silently.
            if(intent.state!="INTENT")return Result.PARTIAL_OR_REVIEW_REQUIRED
            source().use{response->
                if(RangeContract.validate(intent.expectedBytes,0,response.metadata)==null)return Result.PARTIAL_OR_REVIEW_REQUIRED
                val revision=response.sourceRevision
                if(revision!=null) {
                    val asset=checkNotNull(db.ledger().asset(assetId))
                    require(revision.trust==VersionTrust.IMMUTABLE_VERSION && revision.source==lease.sourceId && revision.asset==assetId &&
                        revision.version.isNotBlank() && !asset.identityAmbiguous && revision.version==asset.strongVersion)
                }
                if(cancelled())return Result.PARTIAL_OR_REVIEW_REQUIRED
                journal.reserveAllocation(lease,intent.id)
                val dest=createOwnedPending(db.ledger().replica(assetId)!!.relativePath)
                val before=dest.inspect()
                require(before.locator==dest.locator && before.pending && before.bytes==0L)
                journal.attach(lease,intent.id,dest.locator)
                val copy=CheckedCopy.copy(intent.expectedBytes,0,response.metadata,response::input,dest::output,
                    {n->dest.sync();journal.checkpoint(lease,intent.id,n);progress(n)},cancelled,
                    durablePrefix=revision?.let { trusted->{n,sha->ResumeRepository(db).record(lease,intent.id,n,sha,trusted)} })
                if(copy.outcome!=CheckedCopy.Outcome.COPIED)return Result.PARTIAL_OR_REVIEW_REQUIRED
                val local=dest.inspect()
                val readable=CheckedCopy.readBack(intent.expectedBytes,checkNotNull(copy.digest),dest::input)
                val afterRead=dest.inspect()
                if(afterRead!=local || !readable || cancelled())return Result.PARTIAL_OR_REVIEW_REQUIRED
                val receipt=TransferReceipt(intent.expectedBytes,0,copy.received,response.metadata,false,false,true,true,true,readable)
                val integrity=IntegrityRepository(db).recordTransfer(lease,assetId,local,receipt)
                journal.preparePublication(lease,intent.id,integrity)
                if(cancelled())return Result.PARTIAL_OR_REVIEW_REQUIRED
                dest.publish()
                journal.observePublished(lease,intent.id,dest.inspect())
                // Source identity remains independent and unknown unless separately proven.
                Result.TRANSFERRED_UNVERIFIED
            }
        }catch(_:Exception){Result.PARTIAL_OR_REVIEW_REQUIRED}
    }
}
