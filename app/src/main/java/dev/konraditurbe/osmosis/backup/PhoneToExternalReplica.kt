package dev.konraditurbe.osmosis.backup

import android.content.ContentResolver
import android.net.Uri
import dev.konraditurbe.osmosis.ledger.EnumerationLease
import dev.konraditurbe.osmosis.ledger.LedgerDatabase

/**
 * The Android effect bridge for a verified local phone receipt. The caller supplies the exact
 * owned phone URI and its measured hash; neither filenames nor historic MediaStore rows are used
 * to adopt a source. A failed copy leaves the `.part` object and records no SSD verification.
 */
class PhoneToExternalReplica(private val resolver: ContentResolver, private val database: LedgerDatabase) {
    fun replicate(lease: EnumerationLease, assetId: String, phoneUri: Uri, phoneProof: ReplicaProof,
        destinationId: String, treeUri: Uri, relativePath: String, mime: String, cancelled: () -> Boolean = { false }): ReplicaVerification.Result {
        val destination = database.ledger().storageDestination(destinationId)
            ?: return ReplicaVerification.Result.Rejected("SSD_NOT_CONFIGURED")
        if (destination.state != "AVAILABLE" || destination.treeUri != treeUri.toString())
            return ReplicaVerification.Result.Rejected("SSD_UNAVAILABLE_OR_CHANGED")
        val grant=resolver.persistedUriPermissions.any { it.uri==treeUri && it.isReadPermission && it.isWritePermission }
        val availability=ExternalStoragePolicy.assess(grant,SafDestinationProbe.availableBytes(resolver,treeUri),phoneProof.bytes)
        if(availability!=ExternalStorageAvailability.AVAILABLE) {
            ReplicaEvidenceRepository(database).availability(destinationId,false,availability.name)
            return ReplicaVerification.Result.Rejected("SSD_${availability.name}")
        }
        val evidence=ReplicaEvidenceRepository(database)
        val operation=try { evidence.beginOperation(lease,assetId,destinationId,phoneProof) }
        catch (_: Exception) { return ReplicaVerification.Result.Incomplete(0,"STALE_OR_UNAVAILABLE") }
        val pending = try { SafPendingReplica.create(resolver, treeUri, relativePath, mime) }
        catch (_: Exception) { evidence.finishOperation(lease,operation,false);return ReplicaVerification.Result.Incomplete(0, "SSD_ALLOCATION_UNAVAILABLE") }
        try { evidence.attachOperation(lease,operation,pending.locator) }
        catch (_: Exception) { return ReplicaVerification.Result.Incomplete(0,"STALE_OR_UNAVAILABLE") }
        val result = ReplicaVerification.copy(phoneProof,
            { resolver.openInputStream(phoneUri) ?: throw java.io.FileNotFoundException("PHONE_SOURCE_UNAVAILABLE") }, pending, cancelled) { bytes ->
                evidence.checkpointOperation(lease,operation,bytes)
            }
        if (result is ReplicaVerification.Result.Verified) {
            // The repository repeats epoch/destination validation in one transaction before evidence append.
            evidence.recordVerified(lease, assetId, destinationId, pending.locator, result.proof)
            evidence.finishOperation(lease,operation,true)
        } else {
            evidence.finishOperation(lease,operation,false)
        }
        return result
    }
}
