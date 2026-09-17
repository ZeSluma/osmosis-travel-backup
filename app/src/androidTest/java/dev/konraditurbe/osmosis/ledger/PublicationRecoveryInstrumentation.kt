package dev.konraditurbe.osmosis.ledger

import android.content.Context
import dev.konraditurbe.osmosis.integrity.*
import java.time.Instant
import java.time.ZoneId

object PublicationRecoveryInstrumentation {
    fun verify(context:Context) {
        val name="publication-recovery-${System.nanoTime()}.db"
        var db=LedgerDatabase.open(context,name)
        try {
            val now=Instant.parse("2026-09-17T12:00:00Z")
            for(fault in listOf("none","already-public","revision","locator","length","unreadable","publish","post-publish","stale","partial")) {
                var repo=LedgerRepository(db)
                val remote=RemoteAsset("fake","test.MP4",100,classification=AssetClass.KNOWN_REQUIRED)
                val old=repo.begin("synthetic-recovery-$fault","one","fake",now)
                repo.reconcile(old,listOf(remote),now,ZoneId.of("UTC"));repo.reconcileLocal(old,LocalInventory(emptyList(),true),now)
                val asset=db.ledger().assets(old.snapshotId).single()
                val journal=AttemptRepository(db);val attempt=journal.begin(old,asset.id)
                journal.reserveAllocation(old,attempt.id);journal.attach(old,attempt.id,"content://synthetic/$fault")
                journal.checkpoint(old,attempt.id,if(fault=="partial")40 else 100)
                if(fault!="partial") {
                    val id=IntegrityRepository(db).recordTransfer(old,asset.id,LocalBinding(attempt.locator ?: "content://synthetic/$fault","revision",100,true,true),
                        TransferReceipt(100,0,100,ResponseMetadata(200,100,null),false,false,true,true,true,true))
                    journal.preparePublication(old,attempt.id,id)
                }
                db.close();db=LedgerDatabase.open(context,name);repo=LedgerRepository(db)
                val current=repo.begin("synthetic-recovery-$fault","two","fake",now)
                repo.reconcile(current,listOf(remote),now,ZoneId.of("UTC"))
                var pending=fault!="already-public";var publishes=0;var opens=0
                val destination=object:PublicationDestination {
                    override fun inspect()=LocalBinding(if(fault=="locator")"wrong" else "content://synthetic/$fault",
                        if(fault=="revision" || fault=="post-publish" && publishes>0)"changed" else "revision",
                        if(fault=="length")99 else 100,fault!="unreadable",pending)
                    override fun publish(){publishes++;if(fault=="publish")throw java.io.IOException();pending=false}
                }
                val result=PublicationRecovery(db).recover(if(fault=="stale")old else current,asset.id,{opens++;destination})
                val success=fault in setOf("none","already-public")
                check(result==if(success)PublicationRecovery.Result.RECOVERED_UNVERIFIED else PublicationRecovery.Result.REVIEW_REQUIRED)
                val retained=db.attempts().get(attempt.id)!!
                check(retained.ownerEpoch==old.epoch && retained.state==if(success)"PUBLISHED" else if(fault=="partial")"PARTIAL" else "PUBLISH_PENDING")
                if(fault in setOf("stale","partial"))check(opens==0)
                if(fault in setOf("already-public","revision","locator","length","unreadable","stale","partial"))check(publishes==0)
                check(db.ledger().replica(asset.id)!!.state!="LOCAL_VERIFIED")
                check(!repo.plan(current.snapshotId).localComplete)
                for(receipt in db.integrity().transfers(asset.id))check(db.integrity().sources(receipt.id).isEmpty())
                if(success)check(PublicationRecovery(db).recover(current,asset.id,{error("repeat IO")})==PublicationRecovery.Result.REVIEW_REQUIRED)
            }
        }finally{db.close()}
    }
}
