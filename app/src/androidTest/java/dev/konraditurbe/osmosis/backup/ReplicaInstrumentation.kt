package dev.konraditurbe.osmosis.backup

import android.app.Instrumentation
import dev.konraditurbe.osmosis.ledger.LedgerDatabase

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
            return "PASS: replica schema migration and destination restart persistence"
        } catch(error:Throwable) { throw IllegalStateException("REPLICA_MIGRATION_$stage",error) }
        finally { db.close(); context.deleteDatabase(name) }
    }
}
