package dev.konraditurbe.osmosis.integrity

enum class RecoveryAction { REVIEW_ORPHAN_INTENT, REVALIDATE_PARTIAL, REVIEW_LENGTH_MISMATCH, REVALIDATE_PUBLICATION, PRESERVE_UNAVAILABLE, PRESERVE_PUBLISHED }
/** Pure recovery disposition. No truncate, delete, overwrite or automatic verified promotion. */
object RecoveryPolicy {
    fun decide(hasJournaledLocator:Boolean,checkpoint:Long,expected:Long,actualLength:Long?,
        published:Boolean,publicationIntent:Boolean):RecoveryAction {
        if(!hasJournaledLocator)return RecoveryAction.REVIEW_ORPHAN_INTENT
        if(actualLength==null)return RecoveryAction.PRESERVE_UNAVAILABLE
        if(expected<=0 || checkpoint<0 || actualLength<0 || checkpoint>expected || actualLength!=checkpoint)
            return RecoveryAction.REVIEW_LENGTH_MISMATCH
        if(published)return if(publicationIntent && actualLength==expected) RecoveryAction.REVALIDATE_PUBLICATION else RecoveryAction.PRESERVE_PUBLISHED
        if(publicationIntent)return RecoveryAction.REVALIDATE_PUBLICATION
        return RecoveryAction.REVALIDATE_PARTIAL
    }
}
