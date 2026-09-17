package dev.konraditurbe.osmosis.ledger

import android.content.Context
import dev.konraditurbe.osmosis.integrity.*
import java.io.*
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId

/** Guarded emulator only. Real process boundary with an exclusively created synthetic private file. */
object ResumeProcessInstrumentation {
    private class Destination(private val file:File):OwnedPendingDestination,ResumablePendingDestination {
        override val locator="synthetic-private:${file.name}"
        var pending=true;private var active:RandomAccessFile?=null
        override fun inspect():LocalBinding {
            val digest=MessageDigest.getInstance("SHA-256")
            file.inputStream().use{input->val b=ByteArray(65536);while(true){val n=input.read(b);if(n<0)break;digest.update(b,0,n)}}
            return LocalBinding(locator,digest.digest().joinToString(""){"%02x".format(it)},file.length(),true,pending)
        }
        override fun input()=file.inputStream()
        private fun writer(offset:Long):OutputStream {
            check(active==null)
            val handle=RandomAccessFile(file,"rw");check(handle.length()==offset);handle.seek(offset);active=handle
            return object:OutputStream(){
                override fun write(b:Int){handle.write(b)}
                override fun write(b:ByteArray,off:Int,len:Int){handle.write(b,off,len)}
                override fun close(){try{handle.close()}finally{active=null}}
            }
        }
        override fun output()=writer(0)
        override fun append(expected:LocalBinding):OutputStream {check(inspect()==expected);return writer(expected.bytes)}
        override fun sync(){checkNotNull(active).fd.sync()}
        override fun publish(){pending=false}
    }
    fun run(context:Context,persist:Boolean):String {
        val prefs=context.getSharedPreferences("synthetic-resume-process",0)
        val id=if(persist)System.nanoTime().toString() else checkNotNull(prefs.getString("id",null))
        require(id.matches(Regex("[0-9]+")))
        val file=File(context.noBackupFilesDir,"synthetic-resume-$id.bin")
        if(persist)check(file.createNewFile()) else check(file.isFile && file.length()==65536L)
        val db=LedgerDatabase.open(context,"synthetic-resume-$id.db")
        try {
            val now=Instant.parse("2026-09-17T12:00:00Z");val repo=LedgerRepository(db)
            val lease=repo.begin("synthetic-process-resume",if(persist)"before" else "after","fixture",now)
            val remote=RemoteAsset("fake","process.MP4",131072,classification=AssetClass.KNOWN_REQUIRED,strongVersion="fixture-version-1")
            repo.reconcile(lease,listOf(remote),now,ZoneId.of("UTC"))
            if(persist)repo.reconcileLocal(lease,LocalInventory(emptyList(),true),now)
            val asset=db.ledger().assets(lease.snapshotId).single();val destination=Destination(file)
            val response=object:TransferResponse {
                override val metadata=ResponseMetadata(if(persist)200 else 206,if(persist)131072 else 65536,if(persist)null else "bytes 65536-131071/131072")
                override val sourceRevision=SourceRevision(lease.sourceId,asset.id,"fixture-version-1",VersionTrust.IMMUTABLE_VERSION)
                override fun input()=ByteArray(65536){42}.inputStream()
                override fun close(){}
            }
            if(persist) {
                check(SingleAssetTransfer(db).start(lease,asset.id,{response},{destination})==SingleAssetTransfer.Result.PARTIAL_OR_REVIEW_REQUIRED)
                val attempt=db.attempts().forAsset(asset.id).single()
                check(attempt.checkpoint==65536L && db.resumes().forAttempt(attempt.id).single().checkpoint==65536L)
                check(prefs.edit().putString("id",id).commit())
            }else {
                check(ResumeTransfer(db).resume(lease,asset.id,destination,{offset->check(offset==65536L);response})==ResumeTransfer.Result.TRANSFERRED_UNVERIFIED)
                check(file.length()==131072L && file.inputStream().use{input->generateSequence{input.read().takeIf{it>=0}}.all{it==42}})
                val dims=IntegrityRepository(db).evaluate(lease,asset.id,destination.inspect())
                check(dims.transferIntegrity==EvidenceResult.CONFIRMED && dims.overall==OverallVerification.UNVERIFIED)
            }
        }finally{db.close()}
        return if(persist)"PASS: synthetic synced prefix and immutable-version receipt persisted for process death"
            else "PASS: independent process safely resumed synthetic prefix; transfer confirmed, overall unverified"
    }
}
