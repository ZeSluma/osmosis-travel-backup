package dev.konraditurbe.osmosis.backup

import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

/** A distinct storage domain is a policy fact, never inferred from a display name or path. */
enum class StorageDomain { PHONE_LOCAL, EXTERNAL_SAF }
enum class ReplicaState { NOT_PRESENT, COPYING, PARTIAL, COPIED_UNVERIFIED, VERIFIED, UNAVAILABLE, PERMISSION_REQUIRED, REVIEW_REQUIRED }

data class ReplicaProof(val bytes: Long, val sha256: String)
data class ReplicaStatus(val state: ReplicaState, val proof: ReplicaProof? = null, val reason: String? = null)

/**
 * A newly-created, private staging object. Implementations must never open an existing final object
 * for writing: preserving an unexpected target is safer than guessing that it is ours.
 */
interface PendingReplica {
    val locator: String
    fun output(): OutputStream
    fun input(): InputStream
    fun sync()
    fun bytes(): Long
    fun finalizeReplica()
}

/**
 * Copies a locally verified phone object into a separately allocated destination and verifies the
 * destination independently. This proves phone-to-destination byte equality, not camera equivalence.
 */
object ReplicaVerification {
    sealed interface Result {
        data class Verified(val proof: ReplicaProof) : Result
        data class Incomplete(val durableBytes: Long, val reason: String) : Result
        data class Rejected(val reason: String) : Result
    }

    fun copy(
        expected: ReplicaProof,
        source: () -> InputStream,
        pending: PendingReplica,
        cancelled: () -> Boolean = { false },
        checkpoint: (Long) -> Unit = {}
    ): Result {
        var copied = 0L
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            source().use { input ->
                pending.output().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        if (cancelled()) return Result.Incomplete(copied, "CANCELLED")
                        val n = input.read(buffer)
                        if (n < 0) break
                        if (n == 0) return Result.Incomplete(copied, "ZERO_LENGTH_READ")
                        if (copied + n > expected.bytes) return Result.Incomplete(copied, "SOURCE_LONGER_THAN_PROOF")
                        output.write(buffer, 0, n)
                        digest.update(buffer, 0, n)
                        copied += n
                        pending.sync()
                        checkpoint(copied)
                    }
                }
            }
            if (cancelled()) return Result.Incomplete(copied, "CANCELLED")
            if (copied != expected.bytes || pending.bytes() != expected.bytes)
                return Result.Incomplete(copied, "LENGTH_MISMATCH")
            val written = hex(digest.digest())
            if (written != expected.sha256) return Result.Rejected("PHONE_PROOF_MISMATCH")
            val readBack = digest(expected.bytes, pending::input) ?: return Result.Incomplete(copied, "DESTINATION_READBACK_FAILED")
            if (readBack != expected.sha256) return Result.Rejected("DESTINATION_CHECKSUM_MISMATCH")
            if (cancelled()) return Result.Incomplete(copied, "CANCELLED")
            pending.finalizeReplica()
            Result.Verified(ReplicaProof(expected.bytes, readBack))
        } catch (_: Exception) {
            Result.Incomplete(copied, "IO_FAILURE")
        }
    }

    fun digest(expectedBytes: Long, source: () -> InputStream): String? = try {
        var count = 0L
        val digest = MessageDigest.getInstance("SHA-256")
        source().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                if (n == 0) return null
                count += n
                if (count > expectedBytes) return null
                digest.update(buffer, 0, n)
            }
        }
        if (count == expectedBytes) hex(digest.digest()) else null
    } catch (_: Exception) { null }

    fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }
}

/** Derived product state; no UI flag can promote either outcome. */
object BackupCompletion {
    fun cameraSyncComplete(requiredPhone: Collection<ReplicaStatus>, inventoryTrusted: Boolean, unknownRequired: Boolean): Boolean =
        inventoryTrusted && !unknownRequired && requiredPhone.all { it.state == ReplicaState.VERIFIED }

    fun redundancyComplete(requiredPhone: Collection<ReplicaStatus>, requiredExternal: Collection<ReplicaStatus>, inventoryTrusted: Boolean, unknownRequired: Boolean): Boolean =
        cameraSyncComplete(requiredPhone, inventoryTrusted, unknownRequired) && requiredExternal.size == requiredPhone.size &&
            requiredExternal.all { it.state == ReplicaState.VERIFIED }

    /** Informational only; deliberately has no side effect. */
    fun safeToClearCamera(redundancyComplete: Boolean, freshSourceRevalidation: Boolean, unknownRequired: Boolean): Boolean =
        redundancyComplete && freshSourceRevalidation && !unknownRequired
}

/** Read-only product projection from backend evidence. It has no setters and cannot trigger cleanup. */
data class BackupProductStatus(
    val inventoryTrusted:Boolean, val unknownRequired:Boolean, val cameraSyncComplete:Boolean,
    val redundancyComplete:Boolean, val safeToClearCamera:Boolean, val phoneVerified:Int, val externalVerified:Int
)

object BackupStatusProjection {
    fun derive(inventoryTrusted:Boolean, unknownRequired:Boolean, phone:Collection<ReplicaStatus>, external:Collection<ReplicaStatus>, freshSourceRevalidation:Boolean):BackupProductStatus {
        val sync=BackupCompletion.cameraSyncComplete(phone,inventoryTrusted,unknownRequired)
        val redundancy=BackupCompletion.redundancyComplete(phone,external,inventoryTrusted,unknownRequired)
        return BackupProductStatus(inventoryTrusted,unknownRequired,sync,redundancy,
            BackupCompletion.safeToClearCamera(redundancy,freshSourceRevalidation,unknownRequired),
            phone.count{it.state==ReplicaState.VERIFIED},external.count{it.state==ReplicaState.VERIFIED})
    }
}
