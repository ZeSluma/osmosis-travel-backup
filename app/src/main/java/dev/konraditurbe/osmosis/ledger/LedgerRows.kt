package dev.konraditurbe.osmosis.ledger

import androidx.room.*

@Entity(tableName = "sources", indices = [Index(value = ["association"], unique = true)])
data class SourceRow(@PrimaryKey val id: String, val association: String, val confidence: String,
    val storageEvidence: String, val ownerEpoch: Long)

@Entity(tableName = "snapshots", foreignKeys = [ForeignKey(entity = SourceRow::class, parentColumns = ["id"], childColumns = ["sourceId"])],
    indices = [Index("sourceId"), Index(value = ["sourceId", "requestId"], unique = true)])
data class SnapshotRow(@PrimaryKey val id: String, val sourceId: String, val requestId: String,
    val ownerEpoch: Long, val startedAt: String, val endedAt: String?, val generation: String?,
    val status: String, val failure: String?, val scope: String)

@Entity(tableName = "recordings", foreignKeys = [ForeignKey(entity = SourceRow::class, parentColumns = ["id"], childColumns = ["sourceId"])], indices = [Index("sourceId")])
data class RecordingRow(@PrimaryKey val id: String, val sourceId: String, val evidence: String,
    val relationshipUncertain: Boolean, val timestamp: String, val timeSource: String, val zoneEvidence: String?,
    val captureDay: String, val fallback: String, val confidence: String, val disagreement: Boolean)

@Entity(tableName = "assets", foreignKeys = [
    ForeignKey(entity = SourceRow::class, parentColumns = ["id"], childColumns = ["sourceId"]),
    ForeignKey(entity = RecordingRow::class, parentColumns = ["id"], childColumns = ["recordingId"])],
    indices = [Index("sourceId"), Index("recordingId"), Index(value = ["sourceId", "fingerprint"], unique = true)])
data class AssetRow(@PrimaryKey val id: String, val sourceId: String, val recordingId: String,
    val fingerprint: String, val storage: String, val remotePath: String, val size: Long?, val remoteTime: String?,
    val mediaType: String, val handleEvidence: String?, val strongVersion: String?, val classification: String,
    val classificationEvidence: String, val identityAmbiguous: Boolean, val lastEpoch: Long)

@Entity(tableName = "members", primaryKeys = ["recordingId", "memberKey"], foreignKeys = [
    ForeignKey(entity = RecordingRow::class, parentColumns = ["id"], childColumns = ["recordingId"]),
    ForeignKey(entity = AssetRow::class, parentColumns = ["id"], childColumns = ["assetId"])], indices = [Index("assetId")])
data class MemberRow(val recordingId: String, val memberKey: String, val assetId: String?, val required: Boolean)

@Entity(tableName = "membership", primaryKeys = ["snapshotId", "assetId"], foreignKeys = [
    ForeignKey(entity = SnapshotRow::class, parentColumns = ["id"], childColumns = ["snapshotId"]),
    ForeignKey(entity = AssetRow::class, parentColumns = ["id"], childColumns = ["assetId"])], indices = [Index("assetId")])
data class MembershipRow(val snapshotId: String, val assetId: String)

@Entity(tableName = "replicas", primaryKeys = ["assetId", "destination"], foreignKeys = [
    ForeignKey(entity = AssetRow::class, parentColumns = ["id"], childColumns = ["assetId"])],
    indices = [Index(value = ["destination", "relativePath"], unique = true)])
data class ReplicaRow(val assetId: String, val destination: String, val relativePath: String,
    val state: String, val committedLength: Long, val ownerEpoch: Long, val localLocator: String? = null,
    @ColumnInfo(defaultValue = "'NOT_SCANNED'") val localPresence: String = "NOT_SCANNED")

@Entity(tableName = "local_candidates", primaryKeys = ["assetId", "locator"], foreignKeys = [
    ForeignKey(entity = AssetRow::class, parentColumns = ["id"], childColumns = ["assetId"])], indices = [Index("locator")])
data class LocalCandidateRow(val assetId: String, val locator: String, val displayName: String,
    val directory: String, val bytes: Long?, val pending: Boolean, val metadataVersion: String,
    val evidence: String, val confidence: String, val status: String, val observedAt: String,
    val ownerEpoch: Long)

@Dao
interface LedgerDao {
    @Upsert fun observation(row: IdentityObservationRow)
    @Query("SELECT * FROM identity_observations WHERE sourceId=:source ORDER BY id") fun observations(source: String): List<IdentityObservationRow>
    @Query("SELECT * FROM assets WHERE sourceId=:source AND size>0 ORDER BY id") fun sourceAssets(source: String): List<AssetRow>
    @Upsert fun captureEvidence(row: CaptureEvidenceRow)
    @Query("SELECT * FROM capture_evidence WHERE assetId=:asset") fun captureEvidence(asset: String): CaptureEvidenceRow?
    @Query("SELECT * FROM sources WHERE association=:association") fun source(association: String): SourceRow?
    @Query("SELECT * FROM sources WHERE id=:id") fun sourceById(id: String): SourceRow?
    @Upsert fun source(row: SourceRow)
    @Query("SELECT * FROM snapshots WHERE sourceId=:source AND requestId=:request") fun snapshot(source: String, request: String): SnapshotRow?
    @Query("SELECT * FROM snapshots WHERE id=:id") fun snapshot(id: String): SnapshotRow?
    @Upsert fun snapshot(row: SnapshotRow)
    @Query("SELECT * FROM recordings WHERE id=:id") fun recording(id: String): RecordingRow?
    @Insert(onConflict = OnConflictStrategy.ABORT) fun recording(row: RecordingRow)
    @Update fun updateRecording(row: RecordingRow)
    @Query("SELECT * FROM assets WHERE id=:id") fun asset(id: String): AssetRow?
    @Upsert fun asset(row: AssetRow)
    @Upsert fun member(row: MemberRow)
    @Query("SELECT * FROM members WHERE recordingId=:id") fun members(id: String): List<MemberRow>
    @Insert(onConflict = OnConflictStrategy.IGNORE) fun membership(row: MembershipRow)
    @Query("SELECT assets.* FROM assets INNER JOIN membership ON assets.id=membership.assetId WHERE membership.snapshotId=:snapshot AND assets.size>0 ORDER BY assets.id") fun assets(snapshot: String): List<AssetRow>
    @Query("SELECT * FROM replicas WHERE assetId=:asset AND destination='PHONE_LOCAL'") fun replica(asset: String): ReplicaRow?
    @Upsert fun replica(row: ReplicaRow)
    @Query("SELECT * FROM local_candidates WHERE assetId=:asset ORDER BY locator") fun localCandidates(asset: String): List<LocalCandidateRow>
    @Query("SELECT COUNT(DISTINCT lc.assetId) FROM local_candidates lc JOIN assets a ON a.id=lc.assetId WHERE lc.locator=:locator AND lc.assetId!=:asset AND a.size>0 AND lc.status NOT IN ('MISSING','UNAVAILABLE')")
    fun otherCandidateOwners(locator: String, asset: String): Int
    @Upsert fun localCandidate(row: LocalCandidateRow)
    @Query("SELECT COUNT(*) FROM assets") fun assetCount(): Int
    @Query("SELECT COUNT(*) FROM recordings") fun recordingCount(): Int
}

/** Latest resolved evidence is separate from frozen destination reservations. No media mutation. */
@Entity(tableName = "capture_evidence", foreignKeys = [ForeignKey(entity = AssetRow::class,
    parentColumns = ["id"], childColumns = ["assetId"])])
data class CaptureEvidenceRow(@PrimaryKey val assetId: String, val timestamp: String, val source: String,
    val zoneEvidence: String?, val captureDay: String, val confidence: String, val fallback: String,
    val reservationDayConflict: Boolean)

/** Immutable observation payload; only explicit version proof can add a non-destructive resolution link. */
@Entity(tableName = "identity_observations", foreignKeys = [ForeignKey(entity = SourceRow::class,
    parentColumns = ["id"], childColumns = ["sourceId"])], indices = [Index("sourceId")])
data class IdentityObservationRow(@PrimaryKey val id: String, val sourceId: String, val snapshotId: String?,
    val remotePath: String, val storage: String, val size: Long?, val remoteTime: String?,
    val mediaType: String, val handleEvidence: String?, val strongVersion: String?,
    val status: String, val resolvedAssetId: String?)
