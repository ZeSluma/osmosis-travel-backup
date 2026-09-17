package dev.konraditurbe.osmosis.ledger

/** Read-only presentation, never lifecycle/verification authority. */
enum class BackupDisplayState { NEW, PARTIAL_REVIEW, TRANSFERRED_UNVERIFIED, EXISTING_UNVERIFIED, REVIEW_REQUIRED }
data class BackupDisplay(val state:BackupDisplayState,val percent:Int=0)
object BackupDisplayPolicy {
    fun resolve(state:String,presence:String,hasLocator:Boolean,committed:Long,expected:Long?,published:Boolean=false):BackupDisplay {
        if(presence in setOf("MISSING","UNAVAILABLE","AMBIGUOUS")) return BackupDisplay(BackupDisplayState.REVIEW_REQUIRED)
        if(state=="PARTIAL" && hasLocator && expected!=null && expected>0 && committed in 1 until expected)
            return BackupDisplay(BackupDisplayState.PARTIAL_REVIEW,((committed.toDouble()/expected)*100).toInt().coerceIn(0,99))
        if(presence=="CHANGED")return BackupDisplay(BackupDisplayState.REVIEW_REQUIRED)
        if(state=="TRANSFERRED_UNVERIFIED")return BackupDisplay(
            if(published && hasLocator && expected!=null && expected>0 && committed==expected) BackupDisplayState.TRANSFERRED_UNVERIFIED
            else BackupDisplayState.REVIEW_REQUIRED)
        if(presence=="PRESENT_UNVERIFIED" && hasLocator)return BackupDisplay(BackupDisplayState.EXISTING_UNVERIFIED)
        if(state in setOf("DISCOVERED","PLANNED") && presence=="ABSENT" && !hasLocator && committed==0L)
            return BackupDisplay(BackupDisplayState.NEW)
        return BackupDisplay(BackupDisplayState.REVIEW_REQUIRED)
    }
}
