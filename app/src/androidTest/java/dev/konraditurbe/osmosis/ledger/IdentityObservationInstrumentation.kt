package dev.konraditurbe.osmosis.ledger

import android.app.Instrumentation
import java.time.Instant
import java.time.ZoneId

/** Synthetic-only entry through the guarded ledger suite. */
object IdentityObservationInstrumentation {
    fun verify(instrumentation: Instrumentation) {
        val context=instrumentation.targetContext;val name="identity-${System.nanoTime()}.db"
        val now=Instant.parse("2026-09-17T18:00:00Z");val zone=ZoneId.of("UTC")
        var db=LedgerDatabase.open(context,name)
        try {
            var repo=LedgerRepository(db)
            val a=RemoteAsset("1","DCIM/a.mp4",100,mediaType="VIDEO",classification=AssetClass.KNOWN_REQUIRED)
            val b=a.copy(path="DCIM/b.mp4",size=200);val c=a.copy(path="DCIM/c.mp4",size=300)
            val originals=listOf(a,b,c)
            val local=LocalInventory(listOf(LocalMediaObservation("content://synthetic/1","a.mp4","Movies/Osmosis/",100,false,"v"),LocalMediaObservation("content://synthetic/2","b.mp4","Movies/Osmosis/",200,false,"v")),true)
            val first=repo.begin("source","first","test",now);repo.reconcile(first,originals,now,zone);repo.reconcileLocal(first,local,now)
            val old=db.ledger().asset(a.identity(first.sourceId))!!
            val weak=old.copy(id="legacy-weak",fingerprint="legacy-weak",size=null)
            db.ledger().asset(weak)
            val replica=db.ledger().replica(old.id)!!
            db.ledger().replica(replica.copy(assetId=weak.id,relativePath="legacy-weak",localLocator=null,localPresence="AMBIGUOUS"))
            db.ledger().localCandidate(db.ledger().localCandidates(old.id).single().copy(assetId=weak.id,status="CONFLICT"))
            db.ledger().replica(replica.copy(localLocator=null,localPresence="AMBIGUOUS",state="NEEDS_REVALIDATION"))
            val paths=originals.associate{it.identity(first.sourceId) to db.ledger().replica(it.identity(first.sourceId))!!.relativePath}
            // Exact pre-upgrade schema4, including historical poisoned candidate claims.
            val schema=org.json.JSONObject(instrumentation.context.assets.open("dev.konraditurbe.osmosis.ledger.LedgerDatabase/4.json").bufferedReader().use{it.readText()}).getJSONObject("database")
            db.openHelper.writableDatabase.execSQL("DROP TABLE identity_observations")
            db.openHelper.writableDatabase.execSQL("UPDATE room_master_table SET identity_hash=? WHERE id=42",arrayOf(schema.getString("identityHash")))
            db.openHelper.writableDatabase.version=4;db.close();db=LedgerDatabase.open(context,name);repo=LedgerRepository(db)
            check(db.openHelper.writableDatabase.version==7 && db.ledger().assetCount()==4)
            check(db.ledger().observations(first.sourceId).single().status=="UNRESOLVED")
            val partial=repo.begin("source","partial","test",now)
            repo.reconcile(partial,listOf(a.copy(size=null)),now,zone)
            repo.reconcile(partial,listOf(a.copy(size=null)),now,zone)
            check(db.ledger().assets(partial.snapshotId).isEmpty() && db.ledger().assetCount()==4)
            check(db.ledger().observations(first.sourceId).size==2)
            repo.finish(partial,now,true,true,true,true)
            check(!repo.plan(partial.snapshotId).localComplete && db.ledger().snapshot(partial.snapshotId)!!.status=="INCOMPLETE")
            val next=repo.begin("source","restored","test",now)
            repo.reconcile(next,originals.reversed(),now,zone);repo.reconcileLocal(next,local,now)
            check(db.ledger().assets(next.snapshotId).size==3 && db.ledger().assetCount()==4)
            check(db.ledger().replica(a.identity(first.sourceId))!!.localLocator=="content://synthetic/1")
            check(db.ledger().replica(b.identity(first.sourceId))!!.localLocator=="content://synthetic/2")
            for((id,path) in paths)check(db.ledger().replica(id)!!.relativePath==path)
            check(db.ledger().observations(first.sourceId).all{it.status=="UNRESOLVED"})
            val plan=repo.plan(next.snapshotId)
            repo.reconcile(next,originals,now,zone);repo.reconcileLocal(next,local,now)
            check(repo.plan(next.snapshotId)==plan)
            repo.finish(next,now,true,true,true,true);check(!repo.plan(next.snapshotId).enumerationComplete)
            db.close();db=LedgerDatabase.open(context,name);repo=LedgerRepository(db)
            check(db.ledger().assetCount()==4 && db.ledger().observations(first.sourceId).size==2)
            check(db.ledger().replica(a.identity(first.sourceId))!!.localLocator=="content://synthetic/1")
            // Proven same-version link is non-destructive; weak name-only observation never links.
            val strong=repo.begin("strong","one","test",now)
            val versioned=a.copy(strongVersion="immutable-object-version-1")
            repo.reconcile(strong,listOf(versioned.copy(size=null)),now,zone)
            repo.reconcile(strong,listOf(versioned),now,zone)
            check(db.ledger().observations(strong.sourceId).single().resolvedAssetId==versioned.identity(strong.sourceId))
            check(db.ledger().observations(strong.sourceId).single().status=="RESOLVED_SAME_VERSION")
        } finally {db.close()}
    }
}
