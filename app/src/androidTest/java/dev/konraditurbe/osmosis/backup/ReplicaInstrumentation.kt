package dev.konraditurbe.osmosis.backup

import android.app.Instrumentation
import dev.konraditurbe.osmosis.ledger.LedgerDatabase
import dev.konraditurbe.osmosis.ledger.LedgerRepository
import dev.konraditurbe.osmosis.ledger.RemoteAsset
import dev.konraditurbe.osmosis.ledger.AssetClass
import java.time.Instant
import java.time.ZoneId

/** Emulator-only migration/restart proof; it does not represent a USB SSD provider. */
object ReplicaInstrumentation {
    fun verify(i: Instrumentation): String {
        val context=i.targetContext; val name="replica-${System.nanoTime()}.db"
        var db=LedgerDatabase.open(context,name)
        var stage="open"
        try {
            // Recreate the exact schema-8 boundary, then let the application migration add v9.
            stage="drop";db.openHelper.writableDatabase.execSQL("DROP TABLE replica_operations")
            val schema=org.json.JSONObject(i.context.assets.open("dev.konraditurbe.osmosis.ledger.LedgerDatabase/8.json").bufferedReader().use{it.readText()}).getJSONObject("database")
            db.openHelper.writableDatabase.execSQL("UPDATE room_master_table SET identity_hash=? WHERE id=42",arrayOf(schema.getString("identityHash")))
            stage="reopen";db.openHelper.writableDatabase.version=8; db.close(); db=LedgerDatabase.open(context,name)
            stage="version";check(db.openHelper.writableDatabase.version==9)
            val repository=ReplicaEvidenceRepository(db)
            stage="register";
            repository.registerDestination("ssd-test","content://synthetic/tree/test",true)
            stage="available";check(db.ledger().storageDestination("ssd-test")?.state=="AVAILABLE")
            stage="restart";db.close(); db=LedgerDatabase.open(context,name)
            stage="persisted";check(db.ledger().storageDestination("ssd-test")?.treeUri=="content://synthetic/tree/test")
            stage="identity";check(runCatching { ReplicaEvidenceRepository(db).registerDestination("ssd-test","content://synthetic/tree/replaced",true) }.isFailure)
            val restarted=ReplicaEvidenceRepository(db)
            stage="partialBegin";val ledger=LedgerRepository(db);val now=Instant.parse("2026-09-18T12:00:00Z")
            val lease=ledger.begin("synthetic-replica","one","fake",now)
            stage="partialReconcile";ledger.reconcile(lease,listOf(RemoteAsset("fake","one.MP4",10,classification=AssetClass.KNOWN_REQUIRED)),now,ZoneId.of("UTC"))
            stage="partialAsset";val asset=db.ledger().assets(lease.snapshotId).single()
            stage="partialOperation";val operation=restarted.beginOperation(lease,asset.id,"ssd-test",ReplicaProof(10,"a".repeat(64)))
            stage="partialAttach";restarted.attachOperation(lease,operation,"content://synthetic/part")
            stage="partialCheckpoint";restarted.checkpointOperation(lease,operation,4)
            stage="partialFinish";restarted.finishOperation(lease,operation,false)
            stage="partialRestart";db.close();db=LedgerDatabase.open(context,name)
            val retained=checkNotNull(db.ledger().replicaOperation(operation))
            check(retained.state=="PARTIAL" && retained.checkpoint==4L && retained.locator=="content://synthetic/part")
            stage="reconcile"
            val fresh=LedgerRepository(db).begin("synthetic-replica","two","fake",now)
            LedgerRepository(db).reconcile(fresh,listOf(RemoteAsset("fake","one.MP4",10,classification=AssetClass.KNOWN_REQUIRED)),now,ZoneId.of("UTC"))
            stage="duplicateFence";check(runCatching {
                ReplicaEvidenceRepository(db).beginOperation(fresh,asset.id,"ssd-test",ReplicaProof(10,"a".repeat(64)))
            }.isFailure)
            ReplicaEvidenceRepository(db).reconcileOperation(fresh,operation,StagedReplicaObservation.MISSING)
            check(db.ledger().replicaOperation(operation)?.state=="MISSING")
            stage="reallocateAfterMissing";check(runCatching {
                ReplicaEvidenceRepository(db).beginOperation(fresh,asset.id,"ssd-test",ReplicaProof(10,"a".repeat(64)))
            }.isSuccess)
            stage="sealComplete";LedgerRepository(db).finish(fresh,now,true,true,true,true)
            stage="completeSnapshot";check(db.ledger().latestCompleteSnapshot()?.id==fresh.snapshotId)
            stage="completeRestart";db.close();db=LedgerDatabase.open(context,name)
            check(db.ledger().latestCompleteSnapshot()?.id==fresh.snapshotId)
            return "PASS: replica migration, durable duplicate fence, partial restart, missing reconciliation and complete-snapshot restart selection"
        } catch(error:Throwable) { throw IllegalStateException("REPLICA_MIGRATION_$stage",error) }
        finally { db.close(); context.deleteDatabase(name) }
    }
}
