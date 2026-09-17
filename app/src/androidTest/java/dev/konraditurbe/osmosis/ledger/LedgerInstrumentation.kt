package dev.konraditurbe.osmosis.ledger

import android.app.Instrumentation
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.time.Instant
import java.time.ZoneId
import org.json.JSONObject

/** Synthetic fixtures in a separate no-backup database, never paired-camera data. */
object LedgerInstrumentation {
    fun run(instrumentation: Instrumentation, phase: String): String {
        check(android.os.Build.HARDWARE == "ranchu" && android.os.Build.MODEL.contains("sdk", ignoreCase = true))
        val context = instrumentation.targetContext
        check(context.getSharedPreferences("osmosis", 0).all.isEmpty())
        val now = Instant.parse("2026-09-17T10:00:00Z")
        val zone = ZoneId.of("Europe/Berlin")
        var stage = "open"
        try {
            if (phase == "integrityPersist") return IntegrityProcessInstrumentation.run(context,true)
            if (phase == "integrityRestore") return IntegrityProcessInstrumentation.run(context,false)
            if (phase == "localPersist") return LocalReconciliationInstrumentation.persist(context)
            if (phase == "localRestore") return LocalReconciliationInstrumentation.restore(context)
            if (phase == "persist") {
                val db = LedgerDatabase.open(context, "gate2-process.db")
                val repo = LedgerRepository(db)
                val lease = repo.begin("synthetic", "process-seed", "fake-store", now)
                repo.reconcile(lease, listOf(RemoteAsset("fake", "unknown.xyz", 100)), now, zone)
                val asset = db.ledger().assets(lease.snapshotId).single()
                repo.recordProgress(lease, asset.id, 25, false)
                val plan = repo.plan(lease.snapshotId)
                check(plan.items.single().action == PlanAction.REVIEW_UNKNOWN)
                db.close()
                return "PASS: synthetic process fixture persisted"
            }
            if (phase == "restore") {
                val db = LedgerDatabase.open(context, "gate2-process.db")
                val repo = LedgerRepository(db)
                val lease = repo.begin("synthetic", "process-seed", "fake-store", now)
                val asset = db.ledger().assets(lease.snapshotId).single()
                check(asset.classification == "UNKNOWN_POTENTIALLY_REQUIRED" && asset.identityAmbiguous)
                val replica = db.ledger().replica(asset.id)!!
                check(replica.committedLength == 25L && replica.state == "PARTIAL")
                check(replica.relativePath.startsWith("2026-09-17/"))
                check(repo.plan(lease.snapshotId).items.single().action == PlanAction.REVIEW_UNKNOWN)
                db.close()
                return "PASS: process restart retains asset, unknown, ambiguity, partial, group, path and plan"
            }
            stage = "credential-migration"
            CredentialInstrumentation.verify(context)
            stage = "phone-staging"
            PhoneStagingInstrumentation.verify(context)
            stage = "single-transfer"
            SingleTransferInstrumentation.verify(context)
            stage = "integrity-evidence"
            IntegrityEvidenceInstrumentation.verify(instrumentation)
            stage = "identity-observation"
            IdentityObservationInstrumentation.verify(instrumentation)
            stage = "filename-time"
            FilenameTimeInstrumentation.verify(instrumentation)
            stage = "local-reconciliation"
            LocalReconciliationInstrumentation.verify(context)
            val db = LedgerDatabase.open(context, "gate2-${System.nanoTime()}.db")
            var repo = LedgerRepository(db)
            stage = "empty"
            val empty = repo.begin("empty", "1", "fake", now)
            repo.finish(empty, now, true, true, true, true)
            check(repo.plan(empty.snapshotId).localComplete)
            val primary = RemoteAsset("fake", "video.mp4", 100, mediaType="VIDEO",classification=AssetClass.KNOWN_REQUIRED,
                classificationEvidence="SYNTHETIC_CATALOG",strongVersion="generation1", recordingKey="recording1",
                relationshipProven=true,requiredMemberKeys=setOf("video.mp4","audio.wav"),membersComplete=true)
            val audio = primary.copy(path="audio.wav",memberKey="audio.wav",mediaType="AUDIO")
            stage = "required-members"
            val first = repo.begin("source", "1", "fake", now)
            repo.reconcile(first,listOf(primary),now,zone)
            check(!repo.plan(first.snapshotId).recordingComplete)
            repo.reconcile(first,listOf(audio),now,zone)
            check(repo.plan(first.snapshotId).recordingComplete)
            check(db.ledger().assets(first.snapshotId).size==2)
            val count = db.ledger().assetCount()
            stage = "idempotence"
            repo.reconcile(first,listOf(primary,audio,primary),now.plusSeconds(86400),ZoneId.of("Asia/Tokyo"))
            check(db.ledger().assetCount()==count)
            val plan = repo.plan(first.snapshotId)
            check(plan==repo.plan(first.snapshotId))
            check(plan.items.size==2 && plan.items.all { it.relativePath.startsWith("2026-09-17/") })
            stage = "partial-and-transfer"
            val id=primary.identity(first.sourceId)
            repo.recordProgress(first,id,40,false)
            check(repo.plan(first.snapshotId).items.any { it.action==PlanAction.RESUME_REVALIDATE })
            check(runCatching { repo.recordProgress(first,id,90,true) }.isFailure)
            repo.recordProgress(first,id,100,true)
            check(repo.plan(first.snapshotId).items.any { it.action==PlanAction.VERIFY_EXISTING })
            check(!repo.plan(first.snapshotId).localComplete)
            stage = "object-recreation"
            repo=LedgerRepository(db)
            check(repo.plan(first.snapshotId).items.any { it.action==PlanAction.VERIFY_EXISTING })
            check(repo.begin("source","1","fake",now)==first)
            stage = "new-and-removed"
            val next=repo.begin("source","2","fake",now)
            val photo=RemoteAsset("fake","photo.jpg",60,classification=AssetClass.KNOWN_REQUIRED)
            val raw=RemoteAsset("fake","photo.dng",80,classification=AssetClass.KNOWN_REQUIRED)
            val metadata=RemoteAsset("fake","record.json",20,classification=AssetClass.KNOWN_REQUIRED,classificationEvidence="PROVEN_RECORDING_METADATA")
            val excluded=RemoteAsset("fake","thumb.scr",5,classification=AssetClass.KNOWN_REGENERABLE_EXCLUDED,classificationEvidence="PROVEN_THUMBNAIL")
            repo.reconcile(next,listOf(primary,photo,raw,metadata,excluded),now,zone)
            check(db.ledger().assets(next.snapshotId).size==5)
            check(db.ledger().asset(audio.identity(first.sourceId))!=null) // never delete absent remote truth
            check(repo.plan(next.snapshotId).items.size==4)
            check(runCatching { repo.reconcile(first,listOf(primary),now,zone) }.isFailure)
            stage = "changed-identity"
            val changed=repo.begin("source","3","fake",now)
            repo.reconcile(changed,listOf(photo.copy(size=61),photo.copy(remoteTime="changed")),now,zone)
            check(db.ledger().assets(changed.snapshotId).size==2)
            check(db.ledger().recordingCount()>=5)
            stage = "uncertain-identical-reuse"
            val reuse=repo.begin("source","4","fake",now)
            val countBeforeReuse=db.ledger().assetCount()
            repo.reconcile(reuse,listOf(photo),now.plusSeconds(86400),ZoneId.of("Asia/Tokyo"))
            check(db.ledger().assetCount()==countBeforeReuse)
            check(repo.plan(reuse.snapshotId).items.single().action==PlanAction.REVALIDATE_IDENTITY)
            check(db.ledger().asset(photo.identity(reuse.sourceId))!!.identityAmbiguous)
            stage = "capture-days-and-parent-group"
            val dates=repo.begin("dates","1","fake",now)
            val dayA=primary.copy(path="original.mp4",memberKey="original.mp4",recordingKey="day-a",requiredMemberKeys=setOf("original.mp4","original.wav"),
                capture=listOf(CaptureCandidate(TimeSource.CAMERA_CAPTURE,local=java.time.LocalDateTime.parse("2026-01-01T23:59:59"),trusted=true)))
            val companion=dayA.copy(path="original.wav",memberKey="original.wav",mediaType="AUDIO",capture=emptyList())
            val dayB=dayA.copy(path="next.mp4",memberKey="next.mp4",recordingKey="day-b",requiredMemberKeys=emptySet(),
                capture=listOf(CaptureCandidate(TimeSource.CAMERA_CAPTURE,local=java.time.LocalDateTime.parse("2026-01-02T00:00:01"),trusted=true)))
            repo.reconcile(dates,listOf(companion,dayB,dayA),now,zone)
            val datePlan=repo.plan(dates.snapshotId)
            check(datePlan.items.count { it.relativePath.startsWith("2026-01-01/") }==2)
            check(datePlan.items.count { it.relativePath.startsWith("2026-01-02/") }==1)
            repo.recordProgress(dates,dayA.identity(dates.sourceId),25,false)
            repo=LedgerRepository(db)
            repo.reconcile(dates,listOf(dayA,companion,dayB),now.plusSeconds(86400),ZoneId.of("Pacific/Honolulu"))
            check(repo.plan(dates.snapshotId).items.map { it.relativePath }==datePlan.items.map { it.relativePath })
            stage = "failure-incomplete"
            repo.finish(reuse,now,true,true,true,true,failed=true)
            check(!repo.plan(reuse.snapshotId).enumerationComplete)
            check(runCatching { repo.reconcile(reuse,listOf(photo),now,zone) }.isFailure)
            stage = "rollback-and-foreign-key"
            val before=db.ledger().assetCount()
            check(runCatching { db.runInTransaction {
                db.ledger().member(MemberRow("missing-recording","missing",null,true))
            } }.isFailure)
            check(db.ledger().assetCount()==before)
            check(runCatching { db.runInTransaction {
                db.ledger().source(SourceRow("rollback","rollback","fake","fake",0))
                db.ledger().member(MemberRow("missing-recording","missing",null,true))
            } }.isFailure)
            check(db.ledger().source("rollback")==null)
            stage = "concurrent-duplicate-request"
            val pool=java.util.concurrent.Executors.newFixedThreadPool(4)
            val futures=(1..8).map { pool.submit<EnumerationLease> { LedgerRepository(db).begin("concurrent","same","fake",now) } }
            val leases=futures.map { it.get() }
            check(leases.distinct().size==1)
            pool.shutdown()
            stage = "sqlite-full-rollback"
            val sql=db.openHelper.writableDatabase
            val pageCount=sql.query("PRAGMA page_count").use { it.moveToFirst();it.getLong(0) }
            var observedFull = false
            check(runCatching { db.runInTransaction {
                sql.query("PRAGMA max_page_count=$pageCount").use { check(it.moveToFirst() && it.getLong(0) == pageCount) }
                try {
                    db.ledger().source(SourceRow("full","full","fake","x".repeat(1_000_000),0))
                } catch (error: android.database.sqlite.SQLiteFullException) {
                    observedFull = true
                    throw error
                }
            } }.isFailure)
            check(observedFull)
            check(db.ledger().source("full")==null)
            check(db.ledger().assetCount()>=before)
            stage = "activity-recreation"
            val activity=instrumentation.startActivitySync(android.content.Intent(context,dev.konraditurbe.osmosis.ui.MainActivity::class.java).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) })
            instrumentation.runOnMainSync { activity.recreate() }
            instrumentation.waitForIdleSync()
            check(LedgerRepository(db).plan(next.snapshotId).items.size==4)
            db.close()
            stage = "corruption-preservation"
            val corrupt=File(context.noBackupFilesDir,"gate2-corrupt-${System.nanoTime()}.db")
            val corruptBytes=ByteArray(4096) { 0x31 }
            corrupt.writeBytes(corruptBytes)
            val broken=LedgerDatabase.open(context,corrupt.name)
            check(runCatching { broken.ledger().assetCount() }.isFailure)
            runCatching { broken.close() }
            check(corrupt.exists() && corrupt.readBytes().contentEquals(corruptBytes))
            stage = "migration"
            val name="gate2-migration-${System.nanoTime()}.db"
            val schema=JSONObject(instrumentation.context.assets.open("dev.konraditurbe.osmosis.ledger.LedgerDatabase/1.json").bufferedReader().use { it.readText() }).getJSONObject("database")
            val sqlite=SQLiteDatabase.openOrCreateDatabase(File(context.noBackupFilesDir,name),null)
            val entities=schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity=entities.getJSONObject(i); val table=entity.getString("tableName")
                sqlite.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}",table))
                val indices=entity.getJSONArray("indices")
                for (j in 0 until indices.length()) sqlite.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}",table))
            }
            val queries=schema.getJSONArray("setupQueries")
            for (i in 0 until queries.length()) sqlite.execSQL(queries.getString(i))
            sqlite.execSQL("INSERT INTO sources VALUES ('migration','association','UNCERTAIN','fake',7)")
            sqlite.execSQL("INSERT INTO recordings VALUES ('group','migration','fake',1,'2026-01-01T12:00','CAMERA_CAPTURE',NULL,'2026-01-01','CAMERA_LOCAL_ZONE_UNKNOWN','UNCERTAIN',0)")
            sqlite.execSQL("INSERT INTO assets VALUES ('asset','migration','group','fp','fake','unknown.xyz',100,NULL,'UNKNOWN',NULL,NULL,'UNKNOWN_POTENTIALLY_REQUIRED','UNCLASSIFIED',1,7)")
            sqlite.execSQL("INSERT INTO members VALUES ('group','member','asset',1)")
            sqlite.execSQL("INSERT INTO replicas VALUES ('asset','PHONE_LOCAL','2026-01-01/unknown.xyz','PARTIAL',25,7)")
            sqlite.version=1; sqlite.close()
            val migrated=LedgerDatabase.open(context,name)
            check(migrated.ledger().source("association")?.ownerEpoch==7L)
            check(migrated.openHelper.writableDatabase.version==6)
            check(migrated.ledger().replica("asset")?.localPresence=="NOT_SCANNED")
            check(migrated.ledger().replica("asset")?.committedLength==25L)
            check(migrated.ledger().replica("asset")?.relativePath=="2026-01-01/unknown.xyz")
            check(migrated.ledger().replica("asset")?.localLocator==null)
            check(migrated.ledger().asset("asset")?.classification=="UNKNOWN_POTENTIALLY_REQUIRED")
            check(migrated.ledger().members("group").single().required)
            migrated.close()
            stage = "migration-two-to-three"
            val name2="gate2-migration2-${System.nanoTime()}.db"
            val schema2=JSONObject(instrumentation.context.assets.open("dev.konraditurbe.osmosis.ledger.LedgerDatabase/2.json").bufferedReader().use { it.readText() }).getJSONObject("database")
            val sqlite2=SQLiteDatabase.openOrCreateDatabase(File(context.noBackupFilesDir,name2),null)
            val entities2=schema2.getJSONArray("entities")
            for(i in 0 until entities2.length()) {
                val entity=entities2.getJSONObject(i); val table=entity.getString("tableName")
                sqlite2.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}",table))
                val indices=entity.getJSONArray("indices")
                for(j in 0 until indices.length()) sqlite2.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}",table))
            }
            val setup2=schema2.getJSONArray("setupQueries")
            for(i in 0 until setup2.length()) sqlite2.execSQL(setup2.getString(i))
            sqlite2.execSQL("INSERT INTO sources VALUES ('migration2','association2','UNCERTAIN','fake',7)")
            sqlite2.execSQL("INSERT INTO recordings VALUES ('group2','migration2','fake',1,'2026-01-01T12:00','CAMERA_CAPTURE',NULL,'2026-01-01','CAMERA_LOCAL_ZONE_UNKNOWN','UNCERTAIN',0)")
            sqlite2.execSQL("INSERT INTO assets VALUES ('asset2','migration2','group2','fp2','fake','unknown.xyz',100,NULL,'UNKNOWN',NULL,NULL,'UNKNOWN_POTENTIALLY_REQUIRED','UNCLASSIFIED',1,7)")
            sqlite2.execSQL("INSERT INTO members VALUES ('group2','member2','asset2',1)")
            sqlite2.execSQL("INSERT INTO replicas VALUES ('asset2','PHONE_LOCAL','2026-01-01/unknown.xyz','PARTIAL',25,7,'content://synthetic/partial')")
            sqlite2.version=2; sqlite2.close()
            val migrated2=LedgerDatabase.open(context,name2)
            check(migrated2.openHelper.writableDatabase.version==6)
            val replica2=checkNotNull(migrated2.ledger().replica("asset2"))
            check(replica2.localPresence=="NOT_SCANNED" && replica2.state=="PARTIAL" && replica2.committedLength==25L)
            check(replica2.localLocator=="content://synthetic/partial" && replica2.relativePath=="2026-01-01/unknown.xyz")
            check(migrated2.ledger().localCandidates("asset2").isEmpty())
            check(migrated2.ledger().members("group2").single().required)
            migrated2.close()
            stage = "unsupported-version-preserved"
            val unsupported=SQLiteDatabase.openDatabase(File(context.noBackupFilesDir,name).absolutePath,null,SQLiteDatabase.OPEN_READWRITE)
            unsupported.version=99;unsupported.close()
            val rejected=LedgerDatabase.open(context,name)
            check(runCatching { rejected.ledger().assetCount() }.isFailure)
            runCatching { rejected.close() }
            val retained=SQLiteDatabase.openDatabase(File(context.noBackupFilesDir,name).absolutePath,null,SQLiteDatabase.OPEN_READONLY)
            check(retained.version==99)
            retained.rawQuery("SELECT committedLength FROM replicas WHERE assetId='asset'",null).use { check(it.moveToFirst() && it.getLong(0)==25L) }
            retained.close()
            return "PASS: empty, required members/audio, idempotence, plans, partial/unverified, recreation, new/removed, photo/RAW/metadata/excluded, identity, fencing, incomplete/failure, FK/rollback, migrations-through-6, local candidates/provider changes, audit privacy/corruption; synthetic provider indexed-size lag observed=${LocalReconciliationInstrumentation.providerSizeLagObserved}"
        } catch (error: Throwable) {
            val localStage=error.message?.takeIf { it.matches(Regex("SYNTHETIC_LOCAL_[A-Z_]+|GATE3_STAGE_[A-Z_]+")) }
            val fixtureLine=error.stackTrace.firstOrNull { it.className in setOf(IdentityObservationInstrumentation::class.java.name,LocalReconciliationInstrumentation::class.java.name) }?.lineNumber
            throw IllegalStateException("GATE2_ASSERTION_$stage${localStage?.let { ":$it" } ?: ""}${fixtureLine?.let { ":fixture_line_$it" } ?: ""}") // no exception text/data
        }
    }
}
