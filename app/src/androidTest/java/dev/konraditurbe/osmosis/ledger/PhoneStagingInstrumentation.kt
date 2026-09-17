package dev.konraditurbe.osmosis.ledger
import android.content.Context
import android.net.Uri
import dev.konraditurbe.osmosis.integrity.*
import java.time.Instant
import java.time.ZoneId

/** Existing empty-emulator guard is mandatory. Only the URI created by this fixture is cleaned up. */
object PhoneStagingInstrumentation {
    fun verifyPublicationRecovery(context:Context) {
        val name="phone-recovery-${System.nanoTime()}.db"
        var db=LedgerDatabase.open(context,name);var created:String?=null
        try {
            val now=Instant.parse("2026-09-17T12:00:00Z")
            val remote=RemoteAsset("fake","gate3-recovery-${System.nanoTime()}.MP4",100,classification=AssetClass.KNOWN_REQUIRED)
            var repo=LedgerRepository(db)
            val lease=repo.begin("synthetic-phone-recovery","one","fake",now)
            repo.reconcile(lease,listOf(remote),now,ZoneId.of("UTC"));repo.reconcileLocal(lease,LocalInventory(emptyList(),true),now)
            val asset=db.ledger().assets(lease.snapshotId).single();val path=db.ledger().replica(asset.id)!!.relativePath
            val response=object:TransferResponse {
                override val metadata=ResponseMetadata(200,100,null)
                override fun input()=ByteArray(100){42}.inputStream()
                override fun close(){}
            }
            val result=SingleAssetTransfer(db).start(lease,asset.id,{response},{p->
                val owned=PhonePendingVideo(context).create(p).also{created=it.locator}
                object:OwnedPendingDestination by owned {
                    override fun publish(){throw java.io.IOException("synthetic publication interruption")}
                }
            })
            check(result==SingleAssetTransfer.Result.PARTIAL_OR_REVIEW_REQUIRED)
            check(db.attempts().forAsset(asset.id).single().state=="PUBLISH_PENDING")
            val before=PhonePendingVideo(context).publicationDestination(checkNotNull(created),path).inspect()
            check(before.pending && before.bytes==100L)
            db.close();db=LedgerDatabase.open(context,name);repo=LedgerRepository(db)
            val recoveredLease=repo.begin("synthetic-phone-recovery","two","fake",now)
            repo.reconcile(recoveredLease,listOf(remote),now,ZoneId.of("UTC"))
            val recovered=PublicationRecovery(db).recover(recoveredLease,asset.id,{uri->PhonePendingVideo(context).publicationDestination(uri,path)})
            check(recovered==PublicationRecovery.Result.RECOVERED_UNVERIFIED)
            val after=PhonePendingVideo(context).publicationDestination(checkNotNull(created),path).inspect()
            check(after==before.copy(pending=false))
            check(db.attempts().forAsset(asset.id).single().state=="PUBLISHED")
            check(IntegrityRepository(db).evaluate(recoveredLease,asset.id,after).overall==OverallVerification.UNVERIFIED)
        }finally{created?.let{context.contentResolver.delete(Uri.parse(it),null,null)};db.close()}
    }
    fun verify(context:Context) {
        val db=LedgerDatabase.open(context,"phone-stage-${System.nanoTime()}.db");var created:String?=null
        try {
            val now=Instant.parse("2026-09-17T12:00:00Z");val repo=LedgerRepository(db)
            val lease=repo.begin("synthetic-phone-stage","one","fake",now)
            repo.reconcile(lease,listOf(RemoteAsset("fake","gate3-synthetic-${System.nanoTime()}.MP4",100,classification=AssetClass.KNOWN_REQUIRED)),now,ZoneId.of("UTC"))
            repo.reconcileLocal(lease,LocalInventory(emptyList(),true),now)
            val asset=db.ledger().assets(lease.snapshotId).single();val path=db.ledger().replica(asset.id)!!.relativePath
            val response=object:TransferResponse {
                override val metadata=ResponseMetadata(200,100,null)
                override fun input()=ByteArray(100){42}.inputStream()
                override fun close(){}
            }
            var phase="CREATE";var inspected=0
            val result=SingleAssetTransfer(db).start(lease,asset.id,{response},{p->
                val destination=PhonePendingVideo(context).create(p).also{created=it.locator}
                object:OwnedPendingDestination by destination {
                    override fun inspect():LocalBinding {inspected++;phase=when(inspected){1->"INSPECT_INITIAL";2->"INSPECT_COPIED";3->"INSPECT_READBACK";else->"INSPECT_PUBLISHED"};return try{destination.inspect().also{phase+=if(it.pending)"_PENDING" else "_PUBLIC"}}catch(_:java.io.FileNotFoundException){phase+="_UNAVAILABLE";throw IllegalStateException()}}
                    override fun output():java.io.OutputStream {phase="OUTPUT";return destination.output()}
                    override fun sync(){phase="SYNC";destination.sync()}
                    override fun input():java.io.InputStream {phase="READBACK";return destination.input()}
                    override fun publish(){phase="PUBLISH";destination.publish()}
                }
            })
            check(result==SingleAssetTransfer.Result.TRANSFERRED_UNVERIFIED){"GATE3_STAGE_$phase"}
            check(db.attempts().forAsset(asset.id).single().state=="PUBLISHED")
            check(db.integrity().transfers(asset.id).single().result=="CONFIRMED")
            check(db.integrity().sources(db.integrity().transfers(asset.id).single().id).isEmpty())
            check(runCatching{PhonePendingVideo(context).create(path)}.isFailure)
            check(db.ledger().replica(asset.id)!!.state=="TRANSFERRED_UNVERIFIED")
            val recovered=PhonePendingVideo(context).publicationDestination(checkNotNull(created),path).inspect()
            check(recovered.bytes==100L && !recovered.pending && recovered.readable)
            check(runCatching{PhonePendingVideo(context).publicationDestination(checkNotNull(created),"2000-01-01/wrong.MP4")}.isFailure)
        }finally{created?.let{context.contentResolver.delete(Uri.parse(it),null,null)};db.close()}
    }
}
