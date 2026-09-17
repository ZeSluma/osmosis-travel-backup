package dev.konraditurbe.osmosis.ledger

import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.Callable

data class EnumerationLease(val sourceId: String, val snapshotId: String, val epoch: Long)

/** Synchronous transaction boundary; called on the one coordinator worker, never the UI thread. */
class LedgerRepository(private val db: LedgerDatabase) {
    private val dao get() = db.ledger()
    private fun <T> transaction(block: () -> T): T = db.runInTransaction(Callable(block))

    fun begin(association: String, requestId: String, storageEvidence: String, now: Instant,
        generation: String? = null): EnumerationLease = transaction {
        require(association.isNotBlank() && requestId.isNotBlank())
        val associationKey = key("association", association)
        val source = dao.source(associationKey) ?: SourceRow(UUID.randomUUID().toString(), associationKey,
            "ASSOCIATION_ONLY", storageEvidence, 0)
        val prior = dao.snapshot(source.id, requestId)
        if (prior != null) return@transaction EnumerationLease(source.id, prior.id, prior.ownerEpoch)
        val epoch = Math.addExact(source.ownerEpoch, 1)
        dao.source(source.copy(ownerEpoch = epoch, storageEvidence = storageEvidence))
        val snapshot = SnapshotRow(UUID.randomUUID().toString(), source.id, requestId, epoch, now.toString(),
            null, generation, "INCOMPLETE", null, "UNPROVEN")
        dao.snapshot(snapshot)
        EnumerationLease(source.id, snapshot.id, epoch)
    }

    private fun requireLease(lease: EnumerationLease): SnapshotRow {
        check(dao.sourceById(lease.sourceId)?.ownerEpoch == lease.epoch) { "STALE_OWNER" }
        val snapshot = checkNotNull(dao.snapshot(lease.snapshotId))
        check(snapshot.sourceId == lease.sourceId && snapshot.ownerEpoch == lease.epoch) { "INVALID_LEASE" }
        return snapshot
    }

    fun reconcile(lease: EnumerationLease, items: List<RemoteAsset>, now: Instant, phoneZone: ZoneId) = transaction {
        val snapshot = requireLease(lease)
        check(snapshot.endedAt == null) { "SNAPSHOT_SEALED" }
        // Reject conflicting duplicates instead of allowing input ordering to determine truth.
        val unique = items.groupBy { it.identity(lease.sourceId) }
        require(unique.values.all { it.distinct().size == 1 }) { "CONFLICTING_ENUMERATION" }
        val groupTimes = items.filter { it.relationshipProven }.groupBy { key(lease.sourceId, it.storage, it.recordingKey) }
            .mapValues { (_, members) ->
                val times = members.map { it.capture }.filter { it.isNotEmpty() }.distinct()
                require(times.size <= 1) { "PARENT_CAPTURE_CONFLICT" }
                times.firstOrNull() ?: emptyList()
            }
        for ((id, duplicates) in unique.toSortedMap()) {
            val item = duplicates.first()
            val existing = dao.asset(id)
            val groupId = if (item.relationshipProven) key(lease.sourceId, item.storage, item.recordingKey) else key("ungrouped", id)
            // Later uncertain relationship claims cannot relocate an allocated original.
            require(existing == null || existing.recordingId == groupId) { "RELATIONSHIP_REVALIDATION_REQUIRED" }
            var recording = dao.recording(groupId)
            if (recording == null) {
                val capture = CaptureTimeResolver.resolve(groupTimes[groupId] ?: item.capture, now, phoneZone)
                recording = RecordingRow(groupId, lease.sourceId,
                    if (item.relationshipProven) "ENUMERATOR_PROVEN" else "UNRESOLVED", !item.membersComplete,
                    capture.timestamp, capture.source, capture.zone, capture.day, capture.fallback, capture.confidence, capture.disagreement)
                dao.recording(recording)
            }
            if (recording.relationshipUncertain && item.membersComplete && item.relationshipProven) {
                recording = recording.copy(relationshipUncertain = false)
                dao.updateRecording(recording)
            }
            val ambiguous = item.strongVersion == null
            val asset = AssetRow(id, lease.sourceId, groupId, id, item.storage, item.path, item.size, item.remoteTime,
                item.mediaType, item.handle, item.strongVersion, item.classification.name, item.classificationEvidence,
                ambiguous, lease.epoch)
            // Do not silently reclassify an already known required original as disposable.
            require(existing == null || existing.classification == asset.classification) { "CLASSIFICATION_REVIEW_REQUIRED" }
            dao.asset(asset)
            for (member in item.requiredMemberKeys.sorted()) {
                if (dao.members(groupId).none { it.memberKey == member }) dao.member(MemberRow(groupId, member, null, true))
            }
            val oldMember = dao.members(groupId).firstOrNull { it.memberKey == item.memberKey }
            require(oldMember?.assetId == null || oldMember.assetId == id) { "MEMBER_VERSION_CONFLICT" }
            dao.member(MemberRow(groupId, item.memberKey, id, oldMember?.required == true || item.classification == AssetClass.KNOWN_REQUIRED))
            dao.membership(MembershipRow(lease.snapshotId, id))
            val replica = dao.replica(id)
            if (replica == null) {
                dao.replica(ReplicaRow(id, "PHONE_LOCAL", SyncPlanner.relativePath(recording.captureDay, item.path, id),
                    TransferState.DISCOVERED.name, 0, lease.epoch))
            } else if (existing != null && existing.lastEpoch != lease.epoch) {
                val state = TransferState.valueOf(replica.state)
                val next = if (state in setOf(TransferState.PARTIAL, TransferState.TRANSFERRED_UNVERIFIED)) state else TransferState.NEEDS_REVALIDATION
                dao.replica(replica.copy(state = next.name, ownerEpoch = lease.epoch))
            }
        }
    }

    fun finish(lease: EnumerationLease, now: Instant, allPages: Boolean, allStores: Boolean,
        allMembers: Boolean, stableGeneration: Boolean, failed: Boolean = false) = transaction {
        val snapshot = requireLease(lease)
        if (snapshot.endedAt != null) return@transaction
        val complete = allPages && allStores && allMembers && stableGeneration && !failed
        dao.snapshot(snapshot.copy(endedAt = now.toString(), status = if (complete) "COMPLETE" else "INCOMPLETE",
            failure = if (failed) "ENUMERATION_FAILED" else if (!complete) "COVERAGE_UNPROVEN" else null,
            scope = "pages=$allPages;stores=$allStores;members=$allMembers;stable=$stableGeneration"))
    }

    fun plan(snapshotId: String): PlanResult = transaction {
        val snapshot = checkNotNull(dao.snapshot(snapshotId))
        val assets = dao.assets(snapshotId)
        val plans = assets.mapNotNull { asset ->
            val replica = checkNotNull(dao.replica(asset.id))
            SyncPlanner.action(AssetClass.valueOf(asset.classification), TransferState.valueOf(replica.state), asset.identityAmbiguous)
                ?.let { PlanItem(asset.id, it, replica.relativePath) }
        }.sortedBy { it.assetId }
        val completeGroups = assets.map { it.recordingId }.distinct().all { group ->
            dao.recording(group)?.relationshipUncertain == false && dao.members(group).all { !it.required ||
                (it.assetId != null && assets.any { asset -> asset.id == it.assetId }) }
        } && assets.none { it.classification in setOf("UNKNOWN_POTENTIALLY_REQUIRED", "UNSUPPORTED") }
        PlanResult(snapshotId, plans, snapshot.status == "COMPLETE", completeGroups,
            snapshot.status == "COMPLETE" && completeGroups && plans.isEmpty())
    }

    /** No public VERIFIED promotion exists. GATE-3 must provide actual integrity/publication proof. */
    fun recordProgress(lease: EnumerationLease, assetId: String, bytes: Long, finished: Boolean) = transaction {
        requireLease(lease)
        val asset = checkNotNull(dao.asset(assetId))
        require(asset.sourceId == lease.sourceId && asset.lastEpoch == lease.epoch)
        val replica = checkNotNull(dao.replica(assetId))
        require(bytes >= replica.committedLength && (asset.size == null || bytes <= asset.size))
        require(!finished || (asset.size != null && bytes == asset.size))
        dao.replica(replica.copy(committedLength = bytes, ownerEpoch = lease.epoch,
            state = if (finished) "TRANSFERRED_UNVERIFIED" else "PARTIAL"))
    }
}
