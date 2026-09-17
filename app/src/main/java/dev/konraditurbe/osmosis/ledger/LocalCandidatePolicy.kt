package dev.konraditurbe.osmosis.ledger

/** Metadata observations, never source-equivalence or integrity evidence. */
data class LocalMediaObservation(
    val locator: String, val displayName: String, val directory: String, val bytes: Long?,
    val pending: Boolean, val metadataVersion: String
)

enum class LocalInventoryFailure { VERSION_UNAVAILABLE, PROVIDER_UNAVAILABLE, LIMIT_REACHED, FILE_UNAVAILABLE, NONREGULAR_FILE, UNSTABLE_SNAPSHOT, ACCESS_DENIED }
data class LocalInventory(val items: List<LocalMediaObservation>, val accessibleScopeComplete: Boolean,
    val failure: LocalInventoryFailure? = null)
enum class LocalPresence { NOT_SCANNED, ABSENT, PRESENT_UNVERIFIED, AMBIGUOUS, CHANGED, MISSING, UNAVAILABLE }
data class LocalMatch(val media: LocalMediaObservation, val evidence: String, val status: String)
data class LocalAssessment(val presence: LocalPresence, val matches: List<LocalMatch>)

object LocalCandidatePolicy {
    val roots = listOf("Movies/Osmosis/", "Pictures/Osmosis/", "Download/Osmosis/")
    fun inLandingZone(directory: String): Boolean = roots.any { root ->
        directory.startsWith(root) && directory.split('/').none { it == "." || it == ".." }
    }

    fun assess(remotePath: String, expectedBytes: Long?, reservedPath: String,
        inventory: LocalInventory, prior: List<LocalMatch> = emptyList()): LocalAssessment {
        val leaf = remotePath.substringAfterLast('/').substringAfterLast('\\')
        val current = inventory.items.filter { inLandingZone(it.directory) }.distinctBy { it.locator }
        val matches = current.mapNotNull { item ->
            val reserved = roots.any { item.directory + item.displayName == it + reservedPath }
            val priorMatch = prior.firstOrNull { it.media.locator == item.locator }
            if (item.displayName != leaf && !reserved && priorMatch == null) return@mapNotNull null
            val exact = expectedBytes != null && expectedBytes > 0 && item.bytes == expectedBytes && !item.pending
            val evidence = when {
                !exact -> "LOCAL_METADATA_CONFLICT"
                reserved -> "RESERVED_PATH_SIZE_PUBLISHED"
                item.displayName == leaf -> "LEGACY_NAME_SIZE_SCOPE_PUBLISHED"
                else -> "PRIOR_REFERENCE_CHANGED"
            }
            val changed = priorMatch != null && (priorMatch.media != item || priorMatch.status == "CHANGED")
            LocalMatch(item, evidence, when {
                changed -> "CHANGED"
                !exact || evidence == "PRIOR_REFERENCE_CHANGED" -> "CONFLICT"
                else -> "CANDIDATE"
            })
        }.toMutableList()
        for (old in prior) if (matches.none { it.media.locator == old.media.locator }) {
            matches += old.copy(status = if (inventory.accessibleScopeComplete) "MISSING" else "UNAVAILABLE")
        }
        val live = matches.filter { it.status !in setOf("MISSING", "UNAVAILABLE") }
        val presence = when {
            !inventory.accessibleScopeComplete -> LocalPresence.UNAVAILABLE
            live.size > 1 -> LocalPresence.AMBIGUOUS
            live.any { it.status in setOf("CHANGED", "CONFLICT") } -> LocalPresence.CHANGED
            live.size == 1 && matches.any { it.status == "MISSING" } -> LocalPresence.CHANGED
            live.size == 1 -> LocalPresence.PRESENT_UNVERIFIED
            matches.isNotEmpty() -> LocalPresence.MISSING
            else -> LocalPresence.ABSENT
        }
        return LocalAssessment(presence, matches.sortedBy { it.media.locator })
    }
}
