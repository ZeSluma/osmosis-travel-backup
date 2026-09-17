package dev.konraditurbe.osmosis.ledger

import java.security.MessageDigest
import java.time.*

enum class AssetClass { KNOWN_REQUIRED, KNOWN_OPTIONAL, KNOWN_REGENERABLE_EXCLUDED, UNKNOWN_POTENTIALLY_REQUIRED, UNKNOWN_NON_RECORDING, UNSUPPORTED }
enum class TransferState { DISCOVERED, PLANNED, PARTIAL, TRANSFERRED_UNVERIFIED, LOCAL_VERIFIED, FAILED, RETRY_PENDING, NEEDS_REVALIDATION, LOCAL_PRESENT_UNVERIFIED }
enum class PlanAction { DOWNLOAD, RESUME_REVALIDATE, VERIFY_EXISTING, REVALIDATE_IDENTITY, REVIEW_UNKNOWN }
enum class TimeSource { CAMERA_CAPTURE, REMOTE_FILE, VERIFIED_FILENAME, SYNC_FALLBACK }

/** Operational identity only. Hashing is a key encoding, never source-equivalence proof. */
internal fun key(vararg parts: Any?): String = MessageDigest.getInstance("SHA-256")
    .digest(parts.joinToString("") { val s = it?.toString() ?: "<null>"; "${s.length}:$s" }.toByteArray(Charsets.UTF_8))
    .joinToString("") { "%02x".format(it) }

data class CaptureCandidate(
    val source: TimeSource, val local: LocalDateTime? = null, val instant: Instant? = null,
    val offset: ZoneOffset? = null, val zone: ZoneId? = null, val trusted: Boolean = false
)
data class CaptureResolution(val timestamp: String, val source: String, val zone: String?, val day: String,
    val fallback: String, val confidence: String, val disagreement: Boolean)

object CaptureTimeResolver {
    fun resolve(candidates: List<CaptureCandidate>, allocatedAt: Instant, phoneZone: ZoneId): CaptureResolution {
        val usable = candidates.filter { it.trusted && (it.local != null || it.instant != null) }
            .sortedBy { it.source.ordinal }
        fun day(c: CaptureCandidate): LocalDate = c.local?.toLocalDate()
            ?: c.instant!!.atZone(c.offset ?: c.zone ?: ZoneOffset.UTC).toLocalDate()
        val c = usable.firstOrNull() ?: return CaptureResolution(allocatedAt.toString(), "SYNC_FALLBACK",
            phoneZone.id, allocatedAt.atZone(phoneZone).toLocalDate().toString(), "SYNC_TIME_FALLBACK", "UNCERTAIN", false)
        val conflict = usable.any { day(it) != day(c) } ||
            (c.local != null && c.zone != null && c.zone.rules.getValidOffsets(c.local).isEmpty()) ||
            (c.local != null && c.offset != null && c.zone != null && c.offset !in c.zone.rules.getValidOffsets(c.local)) ||
            (c.local != null && c.instant != null && c.offset != null && c.local.toInstant(c.offset) != c.instant)
        val dayEvidence = if (c.local == null && c.offset == null && c.zone == null) usable.firstOrNull { it.local != null } ?: c else c
        val fallback = when {
            dayEvidence !== c -> "SECONDARY_LOCAL_DAY"
            c.offset != null || c.zone != null -> "NONE"
            c.local != null -> "CAMERA_LOCAL_ZONE_UNKNOWN"
            else -> "UTC_DAY_FALLBACK"
        }
        return CaptureResolution((c.local ?: c.instant).toString(), c.source.name,
            (c.offset ?: c.zone)?.toString(), day(dayEvidence).toString(), fallback,
            if (conflict) "CONFLICT" else if (fallback == "NONE") "SOURCE_REPORTED" else "UNCERTAIN", conflict)
    }
}

/** Relationships require explicit enumerator evidence; no filename-based grouping rule. */
data class RemoteAsset(
    val storage: String, val path: String, val size: Long?, val remoteTime: String? = null,
    val mediaType: String = "UNKNOWN", val handle: String? = null,
    val classification: AssetClass = AssetClass.UNKNOWN_POTENTIALLY_REQUIRED,
    val classificationEvidence: String = "UNCLASSIFIED", val strongVersion: String? = null,
    val recordingKey: String? = null, val relationshipProven: Boolean = false,
    val requiredMemberKeys: Set<String> = emptySet(), val memberKey: String = path,
    val membersComplete: Boolean = false, val capture: List<CaptureCandidate> = emptyList()
) {
    init {
        require(path.isNotBlank() && path.length <= 4096 && !path.contains('\u0000'))
        require(size == null || size >= 0)
        require(classificationEvidence.isNotBlank())
        require(!relationshipProven || recordingKey != null)
    }
    fun identity(source: String) = key(source, storage, path, size, remoteTime, mediaType, handle, strongVersion)
}
data class PlanItem(val assetId: String, val action: PlanAction, val relativePath: String)
data class PlanResult(val snapshotId: String, val items: List<PlanItem>, val enumerationComplete: Boolean,
    val recordingComplete: Boolean, val localComplete: Boolean)

object SyncPlanner {
    /** GATE-2 has no authority to mint verification. A remembered VERIFIED label alone is insufficient. */
    fun action(classification: AssetClass, state: TransferState, identityAmbiguous: Boolean,
        currentVerificationProven: Boolean = false, localPresence: LocalPresence = LocalPresence.NOT_SCANNED): PlanAction? {
        if (classification in setOf(AssetClass.KNOWN_REGENERABLE_EXCLUDED, AssetClass.KNOWN_OPTIONAL, AssetClass.UNKNOWN_NON_RECORDING)) return null
        if (classification in setOf(AssetClass.UNKNOWN_POTENTIALLY_REQUIRED, AssetClass.UNSUPPORTED)) return PlanAction.REVIEW_UNKNOWN
        if (localPresence in setOf(LocalPresence.AMBIGUOUS, LocalPresence.CHANGED, LocalPresence.MISSING, LocalPresence.UNAVAILABLE)) return PlanAction.REVALIDATE_IDENTITY
        if (state == TransferState.LOCAL_VERIFIED && currentVerificationProven && !identityAmbiguous) return null
        if (localPresence == LocalPresence.PRESENT_UNVERIFIED) return PlanAction.VERIFY_EXISTING
        if (state == TransferState.PARTIAL) return PlanAction.RESUME_REVALIDATE
        if (state == TransferState.TRANSFERRED_UNVERIFIED) return PlanAction.VERIFY_EXISTING
        if (state == TransferState.LOCAL_PRESENT_UNVERIFIED) return PlanAction.REVALIDATE_IDENTITY
        if (localPresence == LocalPresence.ABSENT && state != TransferState.LOCAL_VERIFIED) return PlanAction.DOWNLOAD
        if (identityAmbiguous || state in setOf(TransferState.LOCAL_VERIFIED, TransferState.NEEDS_REVALIDATION)) return PlanAction.REVALIDATE_IDENTITY
        return PlanAction.DOWNLOAD
    }
    fun relativePath(day: String, sourceName: String, assetId: String): String {
        LocalDate.parse(day)
        val leaf = sourceName.substringAfterLast('/').substringAfterLast('\\')
            .replace(Regex("[^A-Za-z0-9._-]"), "_").take(120).trim('.') .ifBlank { "asset" }
        val dot = leaf.lastIndexOf('.').takeIf { it > 0 } ?: leaf.length
        return "$day/${leaf.substring(0, dot)}-${assetId.take(24)}${leaf.substring(dot)}"
    }
}
