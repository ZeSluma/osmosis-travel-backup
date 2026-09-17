package dev.konraditurbe.osmosis.ledger

import android.app.Instrumentation
import java.time.Instant
import java.time.ZoneId

/** Synthetic database only; caller enforces the empty-emulator guard. */
object FilenameTimeInstrumentation {
    fun verify(instrumentation: Instrumentation) {
        val context=instrumentation.targetContext
        val name="gate2-filename-${System.nanoTime()}.db"
        val db=LedgerDatabase.open(context,name)
        val now=Instant.parse("2026-09-17T16:46:04Z")
        val zone=ZoneId.of("Europe/Berlin")
        try {
            val repo=LedgerRepository(db)
            val old=RemoteAsset("1","DCIM/DJI_20260916143329_0001_D.MP4",38,classification=AssetClass.KNOWN_REQUIRED)
            val third=old.copy(path="DCIM/DJI_20260917184037_0003_D.MP4",size=103)
            val lease=repo.begin("filename-test","1","fake",now)
            repo.reconcile(lease,listOf(old,third),now,zone)
            val before=db.ledger().assets(lease.snapshotId).associate { it.id to db.ledger().replica(it.id)!!.relativePath }
            // Turn this into the actual schema3 fixture, retaining all pre-existing rows.
            val schema=org.json.JSONObject(instrumentation.context.assets.open("dev.konraditurbe.osmosis.ledger.LedgerDatabase/3.json").bufferedReader().use{it.readText()}).getJSONObject("database")
            db.openHelper.writableDatabase.execSQL("DROP TABLE capture_evidence")
            db.openHelper.writableDatabase.execSQL("UPDATE room_master_table SET identity_hash=? WHERE id=42", arrayOf(schema.getString("identityHash")))
            db.openHelper.writableDatabase.version=3
            db.close()
            val upgraded=LedgerDatabase.open(context,name)
            try {
                check(upgraded.openHelper.writableDatabase.version==4)
                val repo2=LedgerRepository(upgraded)
                val next=repo2.begin("filename-test","2","fake",now.plusSeconds(5))
                val items=listOf(old,third).map { it.copy(capture=listOfNotNull(DjiFilenameTime.fromRemotePath(it.path))) }
                repo2.reconcile(next,items,now.plusSeconds(5),ZoneId.of("Pacific/Honolulu"))
                for(asset in upgraded.ledger().assets(next.snapshotId)) {
                    check(upgraded.ledger().replica(asset.id)!!.relativePath==before[asset.id])
                    val evidence=upgraded.ledger().captureEvidence(asset.id)!!
                    check(evidence.source=="DJI_FILENAME" && evidence.zoneEvidence==null)
                    check(!evidence.timestamp.endsWith("Z"))
                    val recording=upgraded.ledger().recording(asset.recordingId)!!
                    if(asset.remotePath==third.path) {
                        check(recording.timestamp=="2026-09-17T18:40:37" && recording.timeSource=="DJI_FILENAME")
                        check(!evidence.reservationDayConflict)
                    } else {
                        check(recording.timeSource=="SYNC_FALLBACK" && recording.captureDay=="2026-09-17")
                        check(evidence.captureDay=="2026-09-16" && evidence.reservationDayConflict)
                    }
                }
            } finally { upgraded.close() }
            val reopened=LedgerDatabase.open(context,name)
            try { check(reopened.ledger().assetCount()==2); for(id in before.keys) check(reopened.ledger().captureEvidence(id)!!.source=="DJI_FILENAME") }
            finally { reopened.close() }
        } finally { if(db.isOpen) db.close() }
    }
}
