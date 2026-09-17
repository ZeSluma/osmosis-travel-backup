package dev.konraditurbe.osmosis.ledger

import android.content.Context
import android.content.ContentValues
import android.provider.MediaStore
import java.time.Instant
import java.time.ZoneId

/** Invoked only behind LedgerInstrumentation's empty-ranchu emulator guard. */
object LocalReconciliationInstrumentation {
    var providerSizeLagObserved: Boolean = false
        private set
    private inline fun <T> withDb(context: Context, name: String, block: (LedgerDatabase) -> T): T {
        val db=LedgerDatabase.open(context,name)
        return try { block(db) } finally { db.close() }
    }
    private val now=Instant.parse("2026-09-17T10:00:00Z")
    private val zone=ZoneId.of("Europe/Berlin")
    private val remote=RemoteAsset("fake","legacy.mp4",3_071_380_142,classification=AssetClass.KNOWN_REQUIRED)
    private val local=LocalMediaObservation("content://media/external_primary/file/123","legacy.mp4","Movies/Osmosis/",3_071_380_142,false,"synthetic-version")

    fun persist(context: Context): String {
        withDb(context,"gate2-local-process.db") { db ->
            val repo=LedgerRepository(db)
            val lease=repo.begin("local-process","1","fake",now)
            repo.reconcile(lease,listOf(remote),now,zone)
            repo.reconcileLocal(lease,LocalInventory(listOf(local),true),now)
            check(repo.plan(lease.snapshotId).items.single().action==PlanAction.VERIFY_EXISTING)
        }
        return "PASS: synthetic candidate reference persisted for process restart"
    }
    fun restore(context: Context): String {
        withDb(context,"gate2-local-process.db") { db ->
            val repo=LedgerRepository(db); val lease=repo.begin("local-process","1","fake",now)
            val asset=db.ledger().assets(lease.snapshotId).single()
            check(db.ledger().replica(asset.id)!!.localLocator==local.locator)
            check(db.ledger().replica(asset.id)!!.state=="LOCAL_PRESENT_UNVERIFIED")
            check(db.ledger().localCandidates(asset.id).single().confidence=="METADATA_ONLY_NOT_SOURCE_PROOF")
            check(repo.plan(lease.snapshotId).items.single().action==PlanAction.VERIFY_EXISTING)
        }
        return "PASS: process restart retains candidate, evidence and unverified plan"
    }
    fun verify(context: Context) {
        val name="gate2-local-${System.nanoTime()}.db"
        withDb(context,name) { db ->
            val repo=LedgerRepository(db); val lease=repo.begin("legacy","1","fake",now)
            repo.reconcile(lease,listOf(remote),now,zone)
            repo.reconcileLocal(lease,LocalInventory(listOf(local),true),now)
            val asset=db.ledger().assets(lease.snapshotId).single()
            val reserved=db.ledger().replica(asset.id)!!.relativePath
            check(db.ledger().replica(asset.id)!!.state=="LOCAL_PRESENT_UNVERIFIED")
            check(db.ledger().replica(asset.id)!!.committedLength==0L)
            check(repo.plan(lease.snapshotId).items.single().action==PlanAction.VERIFY_EXISTING)
            repo.reconcileLocal(lease,LocalInventory(listOf(local),true),now.plusSeconds(1))
            val projection=Gate2ReadOnlyAudit.project(java.io.File(context.noBackupFilesDir,name))
            check(!projection.contains("BLOCKED_READ_ONLY_PROJECTION")) { "SYNTHETIC_LOCAL_AUDIT_QUERY" }
            val audit=org.json.JSONObject(projection)
            check(audit.getInt("schema")==4)
            check(audit.getJSONArray("assets").getJSONObject(0).getString("recomputed_planner_action")=="VERIFY_EXISTING")
            check(!projection.contains(local.locator) && !projection.contains(local.displayName))
            check(db.ledger().localCandidates(asset.id).size==1)
            check(db.ledger().replica(asset.id)!!.relativePath==reserved)
            val newer=repo.begin("legacy","2","fake",now)
            repo.reconcile(newer,listOf(remote),now,zone)
            check(runCatching { repo.reconcileLocal(lease,LocalInventory(emptyList(),true),now) }.isFailure)
            check(db.ledger().localCandidates(asset.id).single().status=="CANDIDATE")
            repo.reconcileLocal(newer,LocalInventory(listOf(local.copy(metadataVersion="changed")),true),now)
            check(db.ledger().replica(asset.id)!!.state=="NEEDS_REVALIDATION")
            check(repo.plan(newer.snapshotId).items.single().action==PlanAction.REVALIDATE_IDENTITY)
            repo.reconcileLocal(newer,LocalInventory(emptyList(),true),now)
            check(db.ledger().localCandidates(asset.id).single().status=="MISSING")
            check(db.ledger().replica(asset.id)!!.localLocator==null)
            check(db.ledger().replica(asset.id)!!.state!="LOCAL_VERIFIED")
            val collision=repo.begin("collision","1","fake",now)
            repo.reconcile(collision,listOf(remote,remote.copy(storage="other")),now,zone)
            repo.reconcileLocal(collision,LocalInventory(listOf(local),true),now)
            check(db.ledger().assets(collision.snapshotId).all { db.ledger().replica(it.id)!!.localPresence=="AMBIGUOUS" })
            check(repo.plan(collision.snapshotId).items.all { it.action==PlanAction.REVALIDATE_IDENTITY })
        }
        val corrupt=java.io.File(context.noBackupFilesDir,"gate2-audit-corrupt-${System.nanoTime()}.db")
        val bytes=ByteArray(4096) { 51 }; corrupt.writeBytes(bytes)
        check(Gate2ReadOnlyAudit.project(corrupt).contains("BLOCKED_READ_ONLY_PROJECTION")) { "SYNTHETIC_LOCAL_CORRUPT_STATUS" }
        check(corrupt.readBytes().contentEquals(bytes)) { "SYNTHETIC_LOCAL_CORRUPT_PRESERVATION" }
        verifyMediaStore(context)
    }

    private fun verifyMediaStore(context: Context) {
        // New synthetic bytes in the empty emulator only; remove only this exact created URI.
        val name="gate2-synthetic-${System.nanoTime()}.bin"
        val values=ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME,name)
            put(MediaStore.MediaColumns.RELATIVE_PATH,"Download/Osmosis/")
            put(MediaStore.MediaColumns.MIME_TYPE,"application/octet-stream")
            put(MediaStore.MediaColumns.IS_PENDING,1)
        }
        val uri=checkNotNull(context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values))
        try {
            context.contentResolver.openOutputStream(uri)!!.use { it.write(ByteArray(32) { 42 }) }
            context.contentResolver.update(uri,ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING,0) },null,null)
            val inventory=LocalMediaInventory(context).read()
            check(inventory.accessibleScopeComplete)
            val observed=inventory.items.single { it.displayName==name }
            check(observed.bytes==32L && !observed.pending)
            val candidate=LocalCandidatePolicy.assess(name,32,"unused",inventory)
            check(candidate.presence==LocalPresence.PRESENT_UNVERIFIED)
            withDb(context,"gate2-provider-${System.nanoTime()}.db") { db ->
                val repo=LedgerRepository(db); val lease=repo.begin("provider-fixture","1","fake",now)
                repo.reconcile(lease,listOf(RemoteAsset("fake",name,32,classification=AssetClass.KNOWN_REQUIRED)),now,zone)
                repo.reconcileLocal(lease,inventory,now)
                val asset=db.ledger().assets(lease.snapshotId).single()
                check(db.ledger().replica(asset.id)!!.localLocator==observed.locator)
                check(repo.plan(lease.snapshotId).items.single().action==PlanAction.VERIFY_EXISTING)
                context.contentResolver.openOutputStream(uri,"wt")!!.use { it.write(ByteArray(17) { 41 }) }
                val indexedSize=context.contentResolver.query(uri,arrayOf(MediaStore.MediaColumns.SIZE),null,null,null)!!.use {
                    check(it.moveToFirst()); it.getLong(0)
                }
                providerSizeLagObserved=indexedSize!=17L
                var changedInventory=LocalMediaInventory(context).read()
                // Provider reindexing can overlap a deliberately mutated synthetic
                // file. Incomplete observations must stay safe while it settles.
                if (!changedInventory.accessibleScopeComplete) {
                    repo.reconcileLocal(lease,changedInventory,now)
                    check(db.ledger().replica(asset.id)!!.state=="NEEDS_REVALIDATION")
                }
                for(attempt in 1..10) {
                    if (changedInventory.accessibleScopeComplete) break
                    Thread.sleep(100)
                    changedInventory=LocalMediaInventory(context).read()
                }
                check(changedInventory.accessibleScopeComplete) {
                    "SYNTHETIC_LOCAL_PROVIDER_${changedInventory.failure?.name ?: "UNKNOWN"}"
                }
                repo.reconcileLocal(lease,changedInventory,now)
                val changedPresence=LocalPresence.valueOf(db.ledger().replica(asset.id)!!.localPresence)
                check(changedPresence==LocalPresence.CHANGED) { "SYNTHETIC_LOCAL_PROVIDER_CHANGED_${changedPresence.name}" }
                check(db.ledger().replica(asset.id)!!.state=="NEEDS_REVALIDATION")
                context.contentResolver.delete(uri,null,null)
                repo.reconcileLocal(lease,LocalMediaInventory(context).read(),now)
                check(db.ledger().replica(asset.id)!!.localPresence=="MISSING") { "SYNTHETIC_LOCAL_PROVIDER_MISSING" }
                check(db.ledger().replica(asset.id)!!.localLocator==null)
                check(repo.plan(lease.snapshotId).items.single().action==PlanAction.REVALIDATE_IDENTITY)
            }
        } finally {
            context.contentResolver.delete(uri,null,null)
        }
    }
}
