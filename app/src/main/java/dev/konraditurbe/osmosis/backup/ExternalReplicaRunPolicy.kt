package dev.konraditurbe.osmosis.backup

/** Deliberately has no camera/session input: verified phone receipts are an independent replica source. */
object ExternalReplicaRunPolicy {
    fun maySchedule(destination:ExternalStorageAvailability, verifiedPhoneReceipts:Int):Boolean =
        destination==ExternalStorageAvailability.AVAILABLE && verifiedPhoneReceipts>0
}
