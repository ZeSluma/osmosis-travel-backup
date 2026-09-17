package dev.konraditurbe.osmosis.ledger

import android.content.Context
import dev.konraditurbe.osmosis.core.CameraFile
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.Executors

/** No UI, sockets, credentials or media payloads belong to this adapter. */
object CameraLedgerAdapter {
    fun asset(file: CameraFile): RemoteAsset {
        val classification = when (file.ext) {
            "MP4", "MOV", "JPG", "JPEG", "DNG", "RAW", "HEIC", "WAV" -> AssetClass.KNOWN_REQUIRED
            else -> AssetClass.UNKNOWN_POTENTIALLY_REQUIRED
        }
        return RemoteAsset(file.storage.toString(), file.path, file.sizeBytes.takeIf { it > 0 },
            file.mtimeEpoch.takeIf { it > 0 }?.toString(), file.mediaType.toString(),
            file.opHandle.takeIf { it != 0L }?.toString(), classification, "MANIFEST_PRIMARY_TYPE_POLICY_V1")
        // Filename time is retained in the operational path, but never asserted as trusted Pocket capture time.
        // Baseline manifest does not prove all sidecars, volume continuity or stable object versions.
    }
}

/** Process singleton: one serialized writer, application context only; no GATE-7 execution promise. */
class LedgerCoordinator private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val writer = Executors.newSingleThreadExecutor { task -> Thread(task, "osmosis-ledger") }
    private val repository by lazy { LedgerRepository(LedgerDatabase.open(appContext)) }
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
                repository.finish(lease, endedAt, pagesEnded, false, false, false, failed)
                latestPlan = repository.plan(lease.snapshotId)
                status = "PLANNED_INCOMPLETE_INVENTORY"
                // Bound in-memory sessions; durable generations and assets remain in Room.
                if (inventories.size > 4) inventories.keys.firstOrNull { it != session }?.let { inventories.remove(it); enumerationStarts.remove(it) }
            } catch (_: Exception) {
                latestPlan = null
                status = "LEDGER_REVIEW_REQUIRED"
                // Never emit SQL, operational paths or arbitrary exception text to diagnostics.
            }
        }
    }
    companion object {
        @Volatile private var instance: LedgerCoordinator? = null
        fun get(context: Context): LedgerCoordinator = instance ?: synchronized(this) {
            instance ?: LedgerCoordinator(context).also { instance = it }
        }
    }
}
