package dev.konraditurbe.osmosis.ledger

import androidx.room.*
import dev.konraditurbe.osmosis.integrity.*
import java.util.concurrent.Callable

/** Immutable digest of received-and-synced prefix bytes, bound to a justified immutable source version. */
@Entity(tableName="resume_evidence",foreignKeys=[ForeignKey(entity=TransferAttemptRow::class,parentColumns=["id"],childColumns=["attemptId"])],indices=[Index("attemptId")])
data class ResumeEvidenceRow(@PrimaryKey val id:String,val attemptId:String,val locator:String,val checkpoint:Long,
    val sha256:String,val sourceId:String,val assetId:String,val sourceVersion:String,val ownerEpoch:Long)
@Dao interface ResumeDao {
    @Insert fun insert(row:ResumeEvidenceRow)
    @Query("SELECT * FROM resume_evidence WHERE id=:id") fun get(id:String):ResumeEvidenceRow?
    @Query("SELECT * FROM resume_evidence WHERE attemptId=:attempt ORDER BY checkpoint,id") fun forAttempt(attempt:String):List<ResumeEvidenceRow>
}

class ResumeRepository(private val db:LedgerDatabase) {
    data class Candidate(val attempt:TransferAttemptRow,val evidence:ResumeEvidenceRow,val prefix:PrefixProof)
    private fun <T> tx(block:()->T):T=db.runInTransaction(Callable(block))
    private fun current(lease:EnumerationLease,assetId:String):AssetRow {
        val asset=checkNotNull(db.ledger().asset(assetId))
        check(asset.sourceId==lease.sourceId && asset.lastEpoch==lease.epoch && db.ledger().sourceById(lease.sourceId)?.ownerEpoch==lease.epoch)
        return asset
    }
    /** Call only after sync+journal checkpoint; no evidence is inferred from a historic file size. */
    fun record(lease:EnumerationLease,attemptId:String,checkpoint:Long,sha256:String,source:SourceRevision)=tx {
        val attempt=checkNotNull(db.attempts().get(attemptId));val asset=current(lease,attempt.assetId)
        require(source.trust==VersionTrust.IMMUTABLE_VERSION && source.source==lease.sourceId && source.asset==asset.id &&
            source.version.isNotBlank() && !asset.identityAmbiguous && source.version==asset.strongVersion)
        require(attempt.ownerEpoch==lease.epoch && attempt.state=="PARTIAL" && checkpoint==attempt.checkpoint &&
            checkpoint>0 && checkpoint<=attempt.expectedBytes && sha256.matches(Regex("[0-9a-f]{64}")))
        val replica=checkNotNull(db.ledger().replica(asset.id))
        require(attempt.locator!=null && replica.localLocator==attempt.locator && replica.committedLength==checkpoint)
        val row=ResumeEvidenceRow(key("resume-prefix",attemptId,checkpoint,sha256),attemptId,attempt.locator,checkpoint,sha256,
            lease.sourceId,asset.id,source.version,lease.epoch)
        val old=db.resumes().get(row.id)
        if(old==null)db.resumes().insert(row) else check(old==row)
    }
    /** Measured local binding plus same independently trusted version; never filename-based recovery. */
    fun candidate(lease:EnumerationLease,assetId:String,local:LocalBinding):Candidate?=tx {
        val asset=current(lease,assetId)
        if(asset.identityAmbiguous || asset.strongVersion.isNullOrBlank())return@tx null
        val attempt=db.attempts().forAsset(assetId).singleOrNull() ?: return@tx null
        val replica=db.ledger().replica(assetId) ?: return@tx null
        if(attempt.state!="PARTIAL" || attempt.checkpoint<=0 || attempt.checkpoint>=attempt.expectedBytes ||
            asset.size!=attempt.expectedBytes || local.locator!=attempt.locator || replica.localLocator!=attempt.locator ||
            replica.committedLength!=attempt.checkpoint || local.bytes!=attempt.checkpoint || !local.pending || !local.readable)return@tx null
        val matches=db.resumes().forAttempt(attempt.id).filter{it.checkpoint==attempt.checkpoint}
        // Conflicting receipts at one checkpoint cannot be resolved by picking a convenient digest.
        val evidence=matches.singleOrNull() ?: return@tx null
        if(evidence.locator!=attempt.locator || evidence.assetId!=assetId || evidence.sourceId!=lease.sourceId ||
            evidence.sourceVersion!=asset.strongVersion || evidence.sha256!=local.revision)return@tx null
        Candidate(attempt,evidence,PrefixProof(local,evidence.sha256,
            SourceRevision(lease.sourceId,assetId,evidence.sourceVersion,VersionTrust.IMMUTABLE_VERSION)))
    }
    fun adopt(lease:EnumerationLease,candidate:Candidate)=tx {
        check(candidate(lease,candidate.attempt.assetId,candidate.prefix.binding)==candidate)
        // Current source epoch fences old writers; original immutable receipts keep their original epochs.
        db.attempts().update(candidate.attempt.copy(ownerEpoch=lease.epoch))
    }
}
