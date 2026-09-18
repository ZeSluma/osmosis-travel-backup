package dev.konraditurbe.osmosis.ledger

import android.content.Context
import dev.konraditurbe.osmosis.core.CameraFile
import dev.konraditurbe.osmosis.core.urlPath
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.Executors
import android.net.Uri
import dev.konraditurbe.osmosis.backup.PhoneToExternalReplica
import dev.konraditurbe.osmosis.backup.ReplicaProof
import dev.konraditurbe.osmosis.backup.ReplicaVerification
import dev.konraditurbe.osmosis.integrity.EvidenceResult

/** No UI, sockets, credentials or media payloads belong to this adapter. */
object CameraLedgerAdapter {
    fun asset(file: CameraFile): RemoteAsset {
        val classification = when (file.ext) {
            "MP4", "MOV", "JPG", "JPEG", "DNG", "RAW", "HEIC", "WAV" -> AssetClass.KNOWN_REQUIRED
            else -> AssetClass.UNKNOWN_POTENTIALLY_REQUIRED
        }
        return RemoteAsset(file.storage.toString(), file.path, file.sizeBytes.takeIf { it > 0 },
            file.mtimeEpoch.takeIf { it > 0 }?.toString(), file.mediaType.toString(),
            file.opHandle.takeIf { it != 0L }?.toString(), classification, "MANIFEST_PRIMARY_TYPE_POLICY_V1",
            capture = listOfNotNull(DjiFilenameTime.fromRemotePath(file.path)))
        // Exact supported DJI names establish local calendar evidence only, not timezone/UTC.
        // Baseline manifest does not prove all sidecars, volume continuity or stable object versions.
    }
}

/** Process singleton: one serialized writer, application context only; no GATE-7 execution promise. */
class LedgerCoordinator private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val writer = Executors.newSingleThreadExecutor { task -> Thread(task, "osmosis-ledger") }
    private val database by lazy { LedgerDatabase.open(appContext) }
    private val repository by lazy { LedgerRepository(database) }
    private var latestLease: EnumerationLease? = null
    private var leaseSession: String? = null
    @Volatile private var activeSession: String? = null
    fun newSession(): String = UUID.randomUUID().toString().also { activeSession = it }
    private val enumerationStarts = mutableMapOf<String, Instant>()
    private val inventories = mutableMapOf<String, MutableMap<String, CameraFile>>()
    @Volatile var latestPlan: PlanResult? = null
        private set
    @Volatile var status: String = "NOT_TESTED"
        private set

    fun observe(association: String, session: String, files: List<CameraFile>, pagesEnded: Boolean, failed: Boolean, startedAt: Instant = Instant.now()) {
        val captured = files.toList()
        val endedAt = Instant.now()
        writer.execute {
            try {
                if (session != activeSession) return@execute
                val enumerationStart = enumerationStarts.getOrPut(session) { startedAt }
                val inventory = inventories.getOrPut(session) { linkedMapOf() }
                captured.forEach { inventory["${it.storage}:${it.path}"] = it }
                val lease = repository.begin(association, UUID.randomUUID().toString(), "MOUNT_MAPPING_ONLY", enumerationStart)
                repository.reconcile(lease, inventory.values.map(CameraLedgerAdapter::asset), Instant.now(), ZoneId.systemDefault())
                repository.reconcileLocal(lease, LocalMediaInventory(appContext).read(), Instant.now())
                repository.finish(lease, endedAt, pagesEnded, false, false, false, failed)
                latestPlan = repository.plan(lease.snapshotId)
                latestLease = lease
                leaseSession = session
                status = if(pagesEnded && !failed) "PLANNED_COMPLETE_INVENTORY" else "PLANNED_INCOMPLETE_INVENTORY"
                // Bound in-memory sessions; durable generations and assets remain in Room.
                if (inventories.size > 4) inventories.keys.firstOrNull { it != session }?.let { inventories.remove(it); enumerationStarts.remove(it) }
            } catch (_: Exception) {
                latestPlan = null
                latestLease = null
                status = "LEDGER_REVIEW_REQUIRED"
                // Never emit SQL, operational paths or arbitrary exception text to diagnostics.
            }
        }
    }
    enum class TransferResult { TRANSFERRED_UNVERIFIED, EXISTING_UNVERIFIED, REVIEW_REQUIRED }
    data class PhoneReplicaCandidate(val assetId:String,val locator:String,val proof:ReplicaProof,val finalName:String,val mime:String)

    /** Only an owned, published, independently read-back phone receipt becomes SSD replication work. */
    fun phoneReplicaCandidates(session:String,destinationId:String,result:(List<PhoneReplicaCandidate>)->Unit) {
        writer.execute {
            val candidates=runCatching {
                val lease=checkNotNull(latestLease);check(session==activeSession && session==leaseSession)
                database.ledger().assets(lease.snapshotId).mapNotNull { asset ->
                    val replica=database.ledger().replica(asset.id) ?: return@mapNotNull null
                    if(replica.localLocator==null || asset.size==null) return@mapNotNull null
                    val receipt=database.integrity().transfers(asset.id).lastOrNull {
                        it.locator==replica.localLocator && it.expectedBytes==asset.size && it.result==EvidenceResult.CONFIRMED.name
                    } ?: return@mapNotNull null
                    val published=database.attempts().forAsset(asset.id).any { it.state=="PUBLISHED" && it.locator==replica.localLocator && it.checkpoint==asset.size }
                    if(!published) return@mapNotNull null
                    if(database.ledger().replicaProofs(asset.id,destinationId).any { it.state=="VERIFIED" && it.bytes==asset.size && it.sha256==receipt.localRevision }) return@mapNotNull null
                    val leaf=replica.relativePath.substringAfterLast('/').replace(Regex("[^A-Za-z0-9._-]"),"_")
                    PhoneReplicaCandidate(asset.id,replica.localLocator,ReplicaProof(asset.size,receipt.localRevision),leaf,
                        if(leaf.endsWith(".mov",true))"video/quicktime" else "video/mp4")
                }
            }.getOrDefault(emptyList())
            result(candidates)
        }
    }

    fun replicateToExternal(session:String,candidate:PhoneReplicaCandidate,destinationId:String,tree:Uri,
        cancelled:()->Boolean):ReplicaVerification.Result = writer.submit<ReplicaVerification.Result> {
        if(session!=activeSession || session!=leaseSession || cancelled()) return@submit ReplicaVerification.Result.Incomplete(0,"STALE_SESSION")
        val lease=latestLease ?: return@submit ReplicaVerification.Result.Incomplete(0,"NO_LEASE")
        PhoneToExternalReplica(appContext.contentResolver,database).replicate(lease,candidate.assetId,Uri.parse(candidate.locator),
            candidate.proof,destinationId,tree,candidate.finalName,candidate.mime,cancelled)
    }.get()

    /** Refresh from durable exact-identity rows after observation/transfer; never read media. */
    fun displayStates(session:String,files:List<CameraFile>,result:(Map<String,BackupDisplay>)->Unit) {
        writer.execute {
            val values=runCatching {
                val lease=checkNotNull(latestLease)
                check(session==activeSession && session==leaseSession)
                val members=database.ledger().assets(lease.snapshotId).associateBy{it.id}
                files.associate { file ->
                    val remote=CameraLedgerAdapter.asset(file)
                    val asset=members[remote.identity(lease.sourceId)]
                    val replica=asset?.let{database.ledger().replica(it.id)}
                    val published=asset!=null && replica!=null && database.attempts().forAsset(asset.id).any {
                        it.state=="PUBLISHED" && it.locator==replica.localLocator && it.checkpoint==asset.size
                    }
                    displayKey(file) to if(replica==null) BackupDisplay(BackupDisplayState.REVIEW_REQUIRED)
                    else BackupDisplayPolicy.resolve(replica.state,replica.localPresence,replica.localLocator!=null,replica.committedLength,asset?.size,published)
                }
            }.getOrDefault(emptyMap())
            result(values)
        }
    }

    /** Read-only mapping of a complete trusted plan onto visible files. UI may queue it but never start IO here. */
    fun automaticDownloadPaths(session:String,files:List<CameraFile>,result:(Set<String>)->Unit) {
        writer.execute {
            val paths=runCatching {
                val lease=checkNotNull(latestLease)
                check(session==activeSession && session==leaseSession)
                val plan=checkNotNull(latestPlan)
                val ids=AutomaticBackupPlan.downloadAssetIds(plan)
                files.filter { CameraLedgerAdapter.asset(it).identity(lease.sourceId) in ids }.map { it.path }.toSet()
            }.getOrDefault(emptySet())
            result(paths)
        }
    }

    /** Worker-thread caller only. Observation and transfer mutations share one serialized writer. */
    fun transferOriginal(session:String, file:CameraFile, network:android.net.Network,
        cancelled:()->Boolean, progress:(Long)->Unit):TransferResult = writer.submit<TransferResult> {
        if(session!=activeSession || session!=leaseSession || cancelled()) return@submit TransferResult.REVIEW_REQUIRED
        val lease=latestLease ?: return@submit TransferResult.REVIEW_REQUIRED
        val assetId=CameraLedgerAdapter.asset(file).identity(lease.sourceId)
        if(database.ledger().assets(lease.snapshotId).none{it.id==assetId}) return@submit TransferResult.REVIEW_REQUIRED
        // Explicit retry can finish a durable publication gap without another network request or media write.
        if(AttemptRepository(database).publicationCandidate(lease,assetId)!=null) {
            val destination=dev.konraditurbe.osmosis.integrity.PhonePendingVideo(appContext)
            val path=database.ledger().replica(assetId)!!.relativePath
            val recovered=dev.konraditurbe.osmosis.integrity.PublicationRecovery(database).recover(lease,assetId,
                {locator->destination.publicationDestination(locator,path)},{session!=activeSession || cancelled()})
            latestPlan=repository.plan(lease.snapshotId)
            return@submit if(recovered==dev.konraditurbe.osmosis.integrity.PublicationRecovery.Result.RECOVERED_UNVERIFIED)
                TransferResult.TRANSFERRED_UNVERIFIED else TransferResult.REVIEW_REQUIRED
        }
        val item=repository.plan(lease.snapshotId).items.singleOrNull{it.assetId==assetId}
            ?: return@submit TransferResult.REVIEW_REQUIRED
        if(item.action==PlanAction.VERIFY_EXISTING) return@submit TransferResult.EXISTING_UNVERIFIED
        if(item.action!=PlanAction.DOWNLOAD || !file.isVideo) return@submit TransferResult.REVIEW_REQUIRED
        val debugEvidence = appContext.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
        val evidence:(String)->Unit={message->if(debugEvidence) android.util.Log.i("OsmosisIntegrity",message)}
        val source=dev.konraditurbe.osmosis.integrity.CameraTransferSource(network,evidence)
        val destination=dev.konraditurbe.osmosis.integrity.PhonePendingVideo(appContext)
        var lastEvidenceCheckpoint=0L
        val result=dev.konraditurbe.osmosis.integrity.SingleAssetTransfer(database).start(lease,assetId,
            {source.open(file.urlPath())},
            destination::create,{session!=activeSession || cancelled()},{bytes->
                if(bytes-lastEvidenceCheckpoint>=8L*1024*1024){evidence("DURABLE checkpoint_bytes=$bytes");lastEvidenceCheckpoint=bytes}
                progress(bytes)
            })
        evidence("TRANSFER result=${result.name}")
        latestPlan=repository.plan(lease.snapshotId)
        if(result==dev.konraditurbe.osmosis.integrity.SingleAssetTransfer.Result.TRANSFERRED_UNVERIFIED)
            TransferResult.TRANSFERRED_UNVERIFIED else TransferResult.REVIEW_REQUIRED
    }.get()

    companion object {
        fun displayKey(file:CameraFile):String=CameraLedgerAdapter.asset(file).identity("DISPLAY_ONLY")
        @Volatile private var instance: LedgerCoordinator? = null
        fun get(context: Context): LedgerCoordinator = instance ?: synchronized(this) {
            instance ?: LedgerCoordinator(context).also { instance = it }
        }
    }
}
