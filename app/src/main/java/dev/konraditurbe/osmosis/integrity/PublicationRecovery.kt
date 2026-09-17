package dev.konraditurbe.osmosis.integrity

import dev.konraditurbe.osmosis.ledger.*

interface PublicationDestination {
    fun inspect():LocalBinding
    fun publish()
}

/** Finish only an already integrity-confirmed publication gap. Never allocate or write media bytes. */
class PublicationRecovery(private val db:LedgerDatabase) {
    enum class Result { RECOVERED_UNVERIFIED, REVIEW_REQUIRED }
    fun recover(lease:EnumerationLease,assetId:String,openOwned:(String)->PublicationDestination,
        cancelled:()->Boolean={false}):Result {
        return try {
            val journal=AttemptRepository(db)
            val candidate=journal.publicationCandidate(lease,assetId) ?: return Result.REVIEW_REQUIRED
            if(cancelled())return Result.REVIEW_REQUIRED
            val destination=openOwned(checkNotNull(candidate.attempt.locator))
            val before=destination.inspect()
            if(before.locator!=candidate.attempt.locator || before.revision!=candidate.proof.localRevision ||
                before.bytes!=candidate.attempt.expectedBytes || !before.readable || cancelled() ||
                journal.publicationCandidate(lease,assetId)!=candidate)return Result.REVIEW_REQUIRED
            if(before.pending)destination.publish()
            val after=destination.inspect()
            journal.observeRecoveredPublication(lease,candidate,after)
            Result.RECOVERED_UNVERIFIED
        }catch(_:Exception){Result.REVIEW_REQUIRED}
    }
}
