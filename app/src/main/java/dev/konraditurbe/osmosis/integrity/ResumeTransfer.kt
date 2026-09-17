package dev.konraditurbe.osmosis.integrity

import dev.konraditurbe.osmosis.ledger.*

interface ResumablePendingDestination:ResumeDestination,PublicationDestination

/** A trusted-version adapter must opt in. Pocket currently supplies no such capability. */
class ResumeTransfer(private val db:LedgerDatabase) {
    enum class Result { TRANSFERRED_UNVERIFIED, PARTIAL_OR_REVIEW_REQUIRED }
    fun resume(lease:EnumerationLease,assetId:String,destination:ResumablePendingDestination,
        source:(Long)->TransferResponse,cancelled:()->Boolean={false}):Result {
        return try {
            val resumes=ResumeRepository(db)
            val candidate=resumes.candidate(lease,assetId,destination.inspect()) ?: return Result.PARTIAL_OR_REVIEW_REQUIRED
            if(cancelled())return Result.PARTIAL_OR_REVIEW_REQUIRED
            source(candidate.attempt.checkpoint).use { response ->
                val revision=response.sourceRevision ?: return Result.PARTIAL_OR_REVIEW_REQUIRED
                if(revision!=candidate.prefix.source || RangeContract.validate(candidate.attempt.expectedBytes,candidate.attempt.checkpoint,response.metadata)==null)
                    return Result.PARTIAL_OR_REVIEW_REQUIRED
                resumes.adopt(lease,candidate)
                val journal=AttemptRepository(db)
                val result=GuardedResume.copy(candidate.attempt.expectedBytes,candidate.prefix,revision,response.metadata,response::input,
                    destination,{n->journal.checkpoint(lease,candidate.attempt.id,n)},cancelled,
                    {n,sha->resumes.record(lease,candidate.attempt.id,n,sha,revision)})
                if(result.outcome!=GuardedResume.Outcome.COPIED_UNVERIFIED)return Result.PARTIAL_OR_REVIEW_REQUIRED
                val local=destination.inspect()
                if(local!=result.confirmedBinding || cancelled())return Result.PARTIAL_OR_REVIEW_REQUIRED
                val receipt=TransferReceipt(candidate.attempt.expectedBytes,candidate.attempt.checkpoint,
                    candidate.attempt.expectedBytes-candidate.attempt.checkpoint,response.metadata,true,true,true,true,true,true)
                val integrity=IntegrityRepository(db).recordTransfer(lease,assetId,local,receipt)
                journal.preparePublication(lease,candidate.attempt.id,integrity)
                if(cancelled())return Result.PARTIAL_OR_REVIEW_REQUIRED
                destination.publish();journal.observePublished(lease,candidate.attempt.id,destination.inspect())
                // Independent overall source-equivalence approval remains separate from transport continuity.
                Result.TRANSFERRED_UNVERIFIED
            }
        }catch(_:Exception){Result.PARTIAL_OR_REVIEW_REQUIRED}
    }
}
