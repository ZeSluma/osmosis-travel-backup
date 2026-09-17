package dev.konraditurbe.osmosis.ledger

import android.app.Instrumentation
import dev.konraditurbe.osmosis.integrity.*
import java.time.Instant
import java.time.ZoneId

/** Called only by the existing empty-emulator guard; never operates on phone media. */
object IntegrityEvidenceInstrumentation {
    fun verify(i:Instrumentation) {
        val name="integrity-${System.nanoTime()}.db";val context=i.targetContext
        var db=LedgerDatabase.open(context,name)
        val now=Instant.parse("2026-09-17T12:00:00Z")
        try {
            var ledger=LedgerRepository(db)
            val remote=RemoteAsset("fake","synthetic.mp4",100,classification=AssetClass.KNOWN_REQUIRED)
            val lease=ledger.begin("integrity-fixture","one","fake",now)
            ledger.reconcile(lease,listOf(remote),now,ZoneId.of("UTC"))
            val asset=db.ledger().assets(lease.snapshotId).single()
            val replica=db.ledger().replica(asset.id)!!
            db.ledger().replica(replica.copy(localLocator="content://synthetic/1"))
            // Schema5 through current: no proof is invented from pre-existing replica length/state.
            db.openHelper.writableDatabase.execSQL("DROP TABLE resume_evidence")
            db.openHelper.writableDatabase.execSQL("DROP TABLE transfer_attempts")
            db.openHelper.writableDatabase.execSQL("DROP TABLE source_equivalence")
            db.openHelper.writableDatabase.execSQL("DROP TABLE transfer_integrity")
            val schema=org.json.JSONObject(i.context.assets.open("dev.konraditurbe.osmosis.ledger.LedgerDatabase/5.json").bufferedReader().use{it.readText()}).getJSONObject("database")
            db.openHelper.writableDatabase.execSQL("UPDATE room_master_table SET identity_hash=? WHERE id=42",arrayOf(schema.getString("identityHash")))
            db.openHelper.writableDatabase.version=5;db.close();db=LedgerDatabase.open(context,name)
            check(db.openHelper.writableDatabase.version==7)
            check(db.integrity().transfers(asset.id).isEmpty())
            var evidence=IntegrityRepository(db)
            val local=LocalBinding("content://synthetic/1","revision1",100,true,false)
            val receipt=TransferReceipt(100,0,100,ResponseMetadata(200,100,null),false,false,true,true,true,true)
            val proof=evidence.recordTransfer(lease,asset.id,local,receipt)
            check(evidence.recordTransfer(lease,asset.id,local,receipt)==proof)
            var d=evidence.evaluate(lease,asset.id,local)
            check(d.transferIntegrity==EvidenceResult.CONFIRMED && d.sourceIdentity==EvidenceResult.UNCONFIRMED)
            check(d.overall==OverallVerification.UNVERIFIED)
            check(!CompletionEligibility.cameraSyncComplete(true,true,listOf(d)))
            check(!CompletionEligibility.redundancyComplete(true,true,listOf(d)))
            check(runCatching{evidence.recordSourceVersion(lease,proof,"invented-version")}.isFailure)
            db.close();db=LedgerDatabase.open(context,name);evidence=IntegrityRepository(db)
            check(evidence.evaluate(lease,asset.id,local)==d && db.integrity().transfers(asset.id).size==1)
            check(evidence.evaluate(lease,asset.id,local.copy(revision="changed")).transferIntegrity==EvidenceResult.UNCONFIRMED)
            check(db.integrity().transfers(asset.id).single().result=="CONFIRMED") // Historic positive evidence retained.
            ledger=LedgerRepository(db)
            val next=ledger.begin("integrity-fixture","two","fake",now)
            ledger.reconcile(next,listOf(remote),now,ZoneId.of("UTC"))
            check(runCatching{evidence.recordTransfer(lease,asset.id,local,receipt)}.isFailure)
            d=evidence.evaluate(next,asset.id,local)
            check(d.transferIntegrity==EvidenceResult.CONFIRMED && d.overall==OverallVerification.UNVERIFIED)
            // Independent fake version proof for a separate source, never Pocket capability evidence.
            val strong=ledger.begin("strong-integrity","one","fake",now)
            ledger.reconcile(strong,listOf(remote.copy(strongVersion="immutable-v1")),now,ZoneId.of("UTC"))
            val a=db.ledger().assets(strong.snapshotId).single();val p=db.ledger().replica(a.id)!!
            db.ledger().replica(p.copy(localLocator=local.locator))
            val t=evidence.recordTransfer(strong,a.id,local,receipt)
            evidence.recordSourceVersion(strong,t,"immutable-v1")
            check(evidence.evaluate(strong,a.id,local).overall==OverallVerification.VERIFIED)
            val pending=evidence.evaluate(strong,a.id,local.copy(pending=true))
            check(pending.sourceIdentity==EvidenceResult.CONFIRMED && pending.overall==OverallVerification.UNVERIFIED)
            val newer=ledger.begin("strong-integrity","two","fake",now)
            ledger.reconcile(newer,listOf(remote.copy(strongVersion="immutable-v1")),now,ZoneId.of("UTC"))
            check(evidence.evaluate(newer,a.id,local).overall==OverallVerification.UNVERIFIED)
            check(evidence.evaluate(newer,a.id,local).transferIntegrity==EvidenceResult.CONFIRMED)
            check(db.ledger().replica(a.id)!!.state!="LOCAL_VERIFIED")
            val jobLease=ledger.begin("attempt-fixture","one","fake",now)
            ledger.reconcile(jobLease,listOf(remote),now,ZoneId.of("UTC"))
            ledger.reconcileLocal(jobLease,LocalInventory(emptyList(),true),now)
            val jobAsset=db.ledger().assets(jobLease.snapshotId).single()
            var attempts=AttemptRepository(db)
            val intent=attempts.begin(jobLease,jobAsset.id)
            check(attempts.begin(jobLease,jobAsset.id)==intent)
            db.close();db=LedgerDatabase.open(context,name);attempts=AttemptRepository(db)
            check(db.attempts().get(intent.id)!!.state=="INTENT")
            attempts.reserveAllocation(jobLease,intent.id)
            check(runCatching{attempts.reserveAllocation(jobLease,intent.id)}.isFailure)
            attempts.attach(jobLease,intent.id,"content://synthetic/new")
            attempts.checkpoint(jobLease,intent.id,50)
            check(runCatching{attempts.checkpoint(jobLease,intent.id,49)}.isFailure)
            check(runCatching{attempts.checkpoint(jobLease,intent.id,101)}.isFailure)
            db.close();db=LedgerDatabase.open(context,name);attempts=AttemptRepository(db)
            check(db.attempts().get(intent.id)!!.checkpoint==50L)
            attempts.checkpoint(jobLease,intent.id,100)
            evidence=IntegrityRepository(db)
            val jobLocal=local.copy(locator="content://synthetic/new",pending=true)
            val jobProof=evidence.recordTransfer(jobLease,jobAsset.id,jobLocal,receipt)
            attempts.preparePublication(jobLease,intent.id,jobProof)
            db.close();db=LedgerDatabase.open(context,name);attempts=AttemptRepository(db);evidence=IntegrityRepository(db)
            check(db.attempts().get(intent.id)!!.state=="PUBLISH_PENDING")
            check(runCatching{attempts.observePublished(jobLease,intent.id,jobLocal)}.isFailure)
            attempts.observePublished(jobLease,intent.id,jobLocal.copy(pending=false))
            attempts.observePublished(jobLease,intent.id,jobLocal.copy(pending=false))
            check(evidence.evaluate(jobLease,jobAsset.id,jobLocal.copy(pending=false)).overall==OverallVerification.UNVERIFIED)
            check(db.ledger().replica(jobAsset.id)!!.state=="TRANSFERRED_UNVERIFIED")
            ledger=LedgerRepository(db)
            val restarted=ledger.begin("attempt-fixture","restart","fake",now)
            ledger.reconcile(restarted,listOf(remote),now,ZoneId.of("UTC"))
            check(runCatching{attempts.checkpoint(jobLease,intent.id,100)}.isFailure)
            check(runCatching{attempts.begin(restarted,jobAsset.id)}.isFailure)
        } finally {db.close()}
    }
}
