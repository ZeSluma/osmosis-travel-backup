package dev.konraditurbe.osmosis.integrity

/** These are evidence dimensions, never partial VERIFIED lifecycle states. */
enum class EvidenceResult { UNCONFIRMED, CONFIRMED, FAILED }
enum class OverallVerification { UNVERIFIED, VERIFIED }

data class VerificationDimensions(val transferIntegrity: EvidenceResult, val sourceIdentity: EvidenceResult, val publicationConfirmed: Boolean = true) {
    val overall: OverallVerification get() = if (transferIntegrity == EvidenceResult.CONFIRMED &&
        sourceIdentity == EvidenceResult.CONFIRMED && publicationConfirmed) OverallVerification.VERIFIED else OverallVerification.UNVERIFIED
}

/** Necessary prerequisites only; this never authorizes a camera delete operation. */
object CompletionEligibility {
    fun cameraSyncComplete(inventoryComplete: Boolean, requiredMembersComplete: Boolean,
        required: List<VerificationDimensions>): Boolean = inventoryComplete && requiredMembersComplete &&
        required.all { it.overall == OverallVerification.VERIFIED }
    fun redundancyComplete(cameraSyncComplete: Boolean, requiredIndependentDomainsProven: Boolean,
        replicas: List<VerificationDimensions>): Boolean = cameraSyncComplete && requiredIndependentDomainsProven &&
        replicas.isNotEmpty() && replicas.all { it.overall == OverallVerification.VERIFIED }
    fun safeToClearPrerequisites(inventoryComplete: Boolean, requiredMembersComplete: Boolean,
        required: List<VerificationDimensions>, independentDomainsProven: Boolean,
        replicas: List<VerificationDimensions>, proofsCurrent: Boolean): Boolean =
        proofsCurrent && required.isNotEmpty() &&
            redundancyComplete(cameraSyncComplete(inventoryComplete,requiredMembersComplete,required),independentDomainsProven,replicas)
    // G9 owns additional revocable snapshot/confirmation/retained-set conditions. No delete API here.
}

data class ResponseMetadata(val status: Int, val contentLength: Long?, val contentRange: String?,
    val contentEncoding: String? = null)
data class ValidatedRange(val start: Long, val end: Long, val total: Long) {
    val responseLength: Long get() = end - start + 1
}

/** Exact full-tail requests; reject ambiguous HTTP metadata before any output is opened. */
object RangeContract {
    private val range = Regex("bytes ([0-9]+)-([0-9]+)/([0-9]+)")
    fun validate(expected: Long, offset: Long, response: ResponseMetadata): ValidatedRange? {
        if (expected <= 0 || offset < 0 || offset >= expected) return null
        if (response.contentEncoding != null && !response.contentEncoding.equals("identity", true)) return null
        val wanted = expected - offset
        if (response.contentLength != wanted) return null
        if (response.status == 200) {
            return if (offset == 0L && response.contentRange == null) ValidatedRange(0, expected - 1, expected) else null
        }
        if (response.status != 206) return null // Includes416: never completion evidence.
        val parts = range.matchEntire(response.contentRange ?: return null)?.groupValues ?: return null
        val start = parts[1].toLongOrNull() ?: return null
        val end = parts[2].toLongOrNull() ?: return null
        val total = parts[3].toLongOrNull() ?: return null
        return if (start == offset && total == expected && end == expected - 1 && end >= start)
            ValidatedRange(start, end, total) else null
    }
}

/** File identity/revision is independently observed, not merely the filename or length. */
data class LocalBinding(val locator: String, val revision: String, val bytes: Long, val readable: Boolean, val pending: Boolean)
data class TransferReceipt(val expected: Long, val offset: Long, val received: Long, val response: ResponseMetadata,
    val prefixIntegrityConfirmed: Boolean, val prefixSourceContinuityConfirmed: Boolean,
    val eof: Boolean, val flushed: Boolean, val closed: Boolean, val readBackConfirmed: Boolean)

object IntegrityContract {
    fun transfer(receipt: TransferReceipt, local: LocalBinding): EvidenceResult {
        val range = RangeContract.validate(receipt.expected, receipt.offset, receipt.response) ?: return EvidenceResult.FAILED
        // A resumed concatenation needs proof of its retained prefix and same-source continuation.
        if (receipt.offset > 0 && (!receipt.prefixIntegrityConfirmed || !receipt.prefixSourceContinuityConfirmed))
            return EvidenceResult.UNCONFIRMED
        return if (receipt.received == range.responseLength && local.bytes == receipt.expected &&
            local.locator.isNotBlank() && local.revision.isNotBlank() && local.readable &&
            receipt.eof && receipt.flushed && receipt.closed && receipt.readBackConfirmed)
            EvidenceResult.CONFIRMED else EvidenceResult.FAILED
    }
}
