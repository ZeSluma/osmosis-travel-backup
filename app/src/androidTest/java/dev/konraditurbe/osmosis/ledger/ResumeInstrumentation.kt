package dev.konraditurbe.osmosis.ledger

import android.app.Instrumentation
import dev.konraditurbe.osmosis.integrity.*
import java.io.*
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId

/** Synthetic immutable-version source, not evidence that Pocket supports this contract. */
object ResumeInstrumentation {
    private class Destination:OwnedPendingDestination,ResumablePendingDestination {
        var bytes=byteArrayOf();var pending=true;var appends=0;var inspections=0;var mutateAt=0
        override val locator="content://synthetic/resume"
        override fun inspect():LocalBinding {
            inspections++;if(mutateAt==inspections)bytes[0]=(bytes[0]+1).toByte()
            return LocalBinding(locator,MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)},bytes.size.toLong(),true,pending)
        }
        override fun input()=bytes.inputStream()
        private fun writer()=object:OutputStream(){override fun write(b:Int){bytes+=b.toByte()}}
        override fun output():OutputStream {check(bytes.isEmpty());return writer()}
        override fun append(expected:LocalBinding):OutputStream{check(inspect()==expected);appends++;return writer()}
        override fun sync(){}
        override fun publish(){pending=false}
    }
    fun verify(i:Instrumentation) {
        val context=i.targetContext;val name="resume-${System.nanoTime()}.db"
        var db=LedgerDatabase.open(context,name)
        try {
            val now=Instant.parse("2026-09-17T12:00:00Z")
            for(mode in listOf("unknown","trusted","binding-change")) {
                val trusted=mode!="unknown"
                val association="synthetic-resume-$mode"
                var repo=LedgerRepository(db)
                val remote=RemoteAsset("fake","test.MP4",10,classification=AssetClass.KNOWN_REQUIRED,strongVersion=if(trusted)"immutable1" else null)
                val lease=repo.begin(association,"one","fake",now)
                repo.reconcile(lease,listOf(remote),now,ZoneId.of("UTC"));repo.reconcileLocal(lease,LocalInventory(emptyList(),true),now)
                val asset=db.ledger().assets(lease.snapshotId).single();val dest=Destination()
                val version=if(trusted)SourceRevision(lease.sourceId,asset.id,"immutable1",VersionTrust.IMMUTABLE_VERSION) else null
                fun response(offset:Int,body:ByteArray)=object:TransferResponse {
                    override val metadata=ResponseMetadata(if(offset==0)200 else 206,10L-offset,if(offset==0)null else "bytes $offset-9/10")
                    override val sourceRevision=version
                    override fun input()=body.inputStream()
                    override fun close(){}
                }
                check(SingleAssetTransfer(db).start(lease,asset.id,{response(0,byteArrayOf(0,1,2,3))},{dest})==SingleAssetTransfer.Result.PARTIAL_OR_REVIEW_REQUIRED)
                val attempt=db.attempts().forAsset(asset.id).single()
                check(attempt.checkpoint==4L && dest.bytes.size==4)
                if(!trusted) {
                    // Exact schema6 migration with a real retained partial and no invented resume evidence.
                    check(db.resumes().forAttempt(attempt.id).isEmpty())
                    db.openHelper.writableDatabase.execSQL("DROP TABLE resume_evidence")
                    val schema=org.json.JSONObject(i.context.assets.open("dev.konraditurbe.osmosis.ledger.LedgerDatabase/6.json").bufferedReader().use{it.readText()}).getJSONObject("database")
                    db.openHelper.writableDatabase.execSQL("UPDATE room_master_table SET identity_hash=? WHERE id=42",arrayOf(schema.getString("identityHash")))
                    db.openHelper.writableDatabase.version=6
                } else check(db.resumes().forAttempt(attempt.id).single().checkpoint==4L)
                db.close();db=LedgerDatabase.open(context,name);repo=LedgerRepository(db)
                check(db.openHelper.writableDatabase.version==7)
                val next=repo.begin(association,"two","fake",now)
                repo.reconcile(next,listOf(remote),now,ZoneId.of("UTC"))
                if(!trusted) {
                    check(db.attempts().get(attempt.id)!!.checkpoint==4L && db.resumes().forAttempt(attempt.id).isEmpty())
                    check(ResumeTransfer(db).resume(next,asset.id,dest,{error("untrusted network request")})==ResumeTransfer.Result.PARTIAL_OR_REVIEW_REQUIRED)
                    check(dest.appends==0 && dest.bytes.size==4)
                    continue
                }
                check(runCatching{AttemptRepository(db).checkpoint(lease,attempt.id,5)}.isFailure)
                if(mode=="binding-change") {
                    // Includes append's own handle-binding check; inspection7 is the receipt handoff.
                    dest.inspections=0;dest.mutateAt=7
                    check(ResumeTransfer(db).resume(next,asset.id,dest,{response(4,byteArrayOf(4,5,6,7,8,9))})==ResumeTransfer.Result.PARTIAL_OR_REVIEW_REQUIRED)
                    check(dest.pending && db.integrity().transfers(asset.id).isEmpty())
                    continue
                }
                check(ResumeTransfer(db).resume(next,asset.id,dest,{offset->check(offset==4L);response(4,byteArrayOf(4,5,6))})==ResumeTransfer.Result.PARTIAL_OR_REVIEW_REQUIRED)
                check(dest.bytes.size==7 && db.attempts().get(attempt.id)!!.checkpoint==7L)
                check(db.resumes().forAttempt(attempt.id).map{it.checkpoint}==listOf(4L,7L))
                db.close();db=LedgerDatabase.open(context,name);repo=LedgerRepository(db)
                val third=repo.begin(association,"three","fake",now)
                repo.reconcile(third,listOf(remote),now,ZoneId.of("UTC"))
                check(ResumeTransfer(db).resume(third,asset.id,dest,{offset->check(offset==7L);response(7,byteArrayOf(7,8,9))})==ResumeTransfer.Result.TRANSFERRED_UNVERIFIED)
                check(dest.bytes.contentEquals(ByteArray(10){it.toByte()}) && !dest.pending && dest.appends==2)
                check(db.attempts().get(attempt.id)!!.state=="PUBLISHED")
                check(db.resumes().forAttempt(attempt.id).map{it.checkpoint}==listOf(4L,7L,10L))
                val dimensions=IntegrityRepository(db).evaluate(third,asset.id,dest.inspect())
                check(dimensions.transferIntegrity==EvidenceResult.CONFIRMED && dimensions.overall==OverallVerification.UNVERIFIED)
                check(!repo.plan(third.snapshotId).localComplete)
                check(ResumeTransfer(db).resume(third,asset.id,dest,{error("duplicate request")})==ResumeTransfer.Result.PARTIAL_OR_REVIEW_REQUIRED)
            }
        }finally{db.close()}
    }
}
