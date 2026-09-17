package dev.konraditurbe.osmosis.ledger

import android.content.Context
import dev.konraditurbe.osmosis.integrity.*
import java.time.Instant
import java.time.ZoneId

/** Two instrumentation processes, with host force-stop between them; synthetic Room data only. */
object IntegrityProcessInstrumentation {
    fun run(context:Context,persist:Boolean):String {
        val prefs=context.getSharedPreferences("synthetic-gate3-process",0)
        val name=if(persist)"gate3-process-${System.nanoTime()}.db" else checkNotNull(prefs.getString("db",null))
        val db=LedgerDatabase.open(context,name)
        try {
            val repo=LedgerRepository(db);val now=Instant.parse("2026-09-17T12:00:00Z")
            val lease=repo.begin("synthetic-process","one","fake",now)
            if(persist){
                repo.reconcile(lease,listOf("complete","partial").map{RemoteAsset("fake","$it.MP4",100,classification=AssetClass.KNOWN_REQUIRED)},now,ZoneId.of("UTC"))
                repo.reconcileLocal(lease,LocalInventory(emptyList(),true),now)
                for(asset in db.ledger().assets(lease.snapshotId)){
                    val journal=AttemptRepository(db);val attempt=journal.begin(lease,asset.id)
                    journal.reserveAllocation(lease,attempt.id)
                    journal.attach(lease,attempt.id,"content://synthetic/${asset.remotePath}")
                    val completed=asset.remotePath=="complete.MP4"
                    journal.checkpoint(lease,attempt.id,if(completed)100 else 40)
                    if(completed){
                        val local=LocalBinding("content://synthetic/${asset.remotePath}","synthetic-revision",100,true,true)
                        val proof=IntegrityRepository(db).recordTransfer(lease,asset.id,local,
                            TransferReceipt(100,0,100,ResponseMetadata(200,100,null),false,false,true,true,true,true))
                        journal.preparePublication(lease,attempt.id,proof)
                        // Simulated death in publication gap: no PUBLISHED observation recorded.
                    }
                }
                check(prefs.edit().putString("db",name).commit())
            }else{
                check(db.ledger().assets(lease.snapshotId).size==2)
                for(asset in db.ledger().assets(lease.snapshotId)){
                    val attempt=db.attempts().forAsset(asset.id).single()
                    val replica=db.ledger().replica(asset.id)!!
                    check(attempt.locator==replica.localLocator && replica.state!="LOCAL_VERIFIED")
                    if(asset.remotePath=="complete.MP4"){
                        check(attempt.state=="PUBLISH_PENDING" && attempt.checkpoint==100L)
                        val dims=IntegrityRepository(db).evaluate(lease,asset.id,
                            LocalBinding(attempt.locator!!,"synthetic-revision",100,true,true))
                        check(dims.transferIntegrity==EvidenceResult.CONFIRMED && dims.sourceIdentity==EvidenceResult.UNCONFIRMED && dims.overall==OverallVerification.UNVERIFIED)
                        check(!CompletionEligibility.cameraSyncComplete(true,true,listOf(dims)))
                    }else{
                        check(attempt.state=="PARTIAL" && attempt.checkpoint==40L && replica.committedLength==40L)
                        check(db.integrity().transfers(asset.id).isEmpty())
                    }
                }
                check(!repo.plan(lease.snapshotId).localComplete)
            }
        }finally{db.close()}
        return if(persist)"PASS: synthetic G3 partial and publication-gap evidence persisted"
            else "PASS: process restart preserves journal, independent integrity, source unknown and overall unverified"
    }
}
