package dev.konraditurbe.osmosis.ledger
import android.content.Context
import dev.konraditurbe.osmosis.integrity.*
import java.io.*
import java.time.Instant
import java.time.ZoneId

object SingleTransferInstrumentation {
    fun verify(context:Context) {
        val db=LedgerDatabase.open(context,"single-transfer-${System.nanoTime()}.db")
        try {
            val ledger=LedgerRepository(db);val now=Instant.parse("2026-09-17T12:00:00Z")
            for(fault in listOf("none","range","short","excess","write","sync","close","publish","readback")) {
                val lease=ledger.begin("synthetic-$fault","one","fake",now)
                ledger.reconcile(lease,listOf(RemoteAsset("fake","test.mp4",100,classification=AssetClass.KNOWN_REQUIRED)),now,ZoneId.of("UTC"))
                ledger.reconcileLocal(lease,LocalInventory(emptyList(),true),now)
                val asset=db.ledger().assets(lease.snapshotId).single()
                var created=0;var pending=true;val out=ByteArrayOutputStream()
                val dest=object:OwnedPendingDestination {
                    override val locator="content://synthetic/$fault"
                    override fun output():OutputStream=object:OutputStream(){
                        override fun write(b:Int){if(fault=="write")throw IOException();out.write(b)}
                        override fun close(){if(fault=="close")throw IOException()}
                    }
                    override fun sync(){if(fault=="sync")throw IOException()}
                    override fun inspect()=LocalBinding(locator,"fake-revision",out.size().toLong(),true,pending)
                    override fun input():InputStream=(if(fault=="readback")ByteArray(100){99}else out.toByteArray()).inputStream()
                    override fun publish(){if(fault=="publish")throw IOException();pending=false}
                }
                val response=object:TransferResponse {
                    override val metadata=ResponseMetadata(if(fault=="range")416 else 200,100,null)
                    override fun input():InputStream=ByteArray(when(fault){"short"->99;"excess"->101;else->100}){42}.inputStream()
                    override fun close(){}
                }
                val result=SingleAssetTransfer(db).start(lease,asset.id,{response},{created++;dest})
                check(result==if(fault=="none")SingleAssetTransfer.Result.TRANSFERRED_UNVERIFIED else SingleAssetTransfer.Result.PARTIAL_OR_REVIEW_REQUIRED)
                if(fault=="none") {
                    val evidence=IntegrityRepository(db).evaluate(lease,asset.id,dest.inspect())
                    check(evidence.transferIntegrity==EvidenceResult.CONFIRMED && evidence.overall==OverallVerification.UNVERIFIED)
                    check(!ledger.plan(lease.snapshotId).localComplete)
                    val again=SingleAssetTransfer(db).start(lease,asset.id,{error("duplicate response")},{error("duplicate allocation")})
                    check(again==SingleAssetTransfer.Result.PARTIAL_OR_REVIEW_REQUIRED && created==1)
                } else {
                    check(pending)
                    if(created>0){
                        val retained=out.toByteArray()
                        val again=SingleAssetTransfer(db).start(lease,asset.id,{error("unproven retry response")},{error("unproven retry allocation")})
                        check(again==SingleAssetTransfer.Result.PARTIAL_OR_REVIEW_REQUIRED && created==1)
                        check(retained.contentEquals(out.toByteArray()))
                    }
                }
                check(db.ledger().replica(asset.id)!!.state!="LOCAL_VERIFIED")
            }
        }finally{db.close()}
    }
}
