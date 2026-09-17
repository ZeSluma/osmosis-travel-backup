package dev.konraditurbe.osmosis.integrity

import org.junit.Assert.*
import org.junit.Test
import java.io.*
import java.security.MessageDigest

class GuardedResumeTest {
    private fun hash(bytes:ByteArray)=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
    private val revision=SourceRevision("source","asset","immutable-version",VersionTrust.IMMUTABLE_VERSION)
    private class Dest(initial:ByteArray):ResumeDestination {
        var bytes=initial;var opens=0;var syncFailure=false;var closeFailure=false;var mutateOnAppend=false;var mutateReadBack=false
        override fun inspect()=LocalBinding("owned-uri",bytes.contentHashCode().toString(),bytes.size.toLong(),true,true)
        override fun input():InputStream=ByteArrayInputStream(bytes.copyOf().also { if(mutateReadBack && opens>0)it[0]=(it[0]+1).toByte() })
        override fun append(expected:LocalBinding):OutputStream {
            if(mutateOnAppend)bytes[0]=(bytes[0]+1).toByte()
            check(inspect()==expected);opens++
            return object:OutputStream(){
                override fun write(b:Int){bytes+=b.toByte()}
                override fun write(b:ByteArray,off:Int,len:Int){bytes+=b.copyOfRange(off,off+len)}
                override fun close(){if(closeFailure)throw IOException("fixture")}
            }
        }
        override fun sync(){if(syncFailure)throw IOException("fixture")}
    }
    private fun run(dest:Dest,source:SourceRevision=revision,metadata:ResponseMetadata=ResponseMetadata(206,3,"bytes 3-5/6"),
        tail:ByteArray=byteArrayOf(4,5,6),proof:PrefixProof=PrefixProof(dest.inspect(),hash(dest.bytes),revision),
        checkpoint:(Long)->Unit={},cancelled:()->Boolean={false})=
        GuardedResume.copy(6,proof,source,metadata,{ByteArrayInputStream(tail)},dest,checkpoint,cancelled)

    @Test fun exactResumeRetainsPrefixAndProducesUnverifiedResult(){
        val d=Dest(byteArrayOf(1,2,3));val checkpoints=mutableListOf<Long>()
        val result=run(d,checkpoint={checkpoints+=it})
        assertEquals(GuardedResume.Outcome.COPIED_UNVERIFIED,result.outcome)
        assertArrayEquals(byteArrayOf(1,2,3,4,5,6),d.bytes);assertEquals(listOf(6L),checkpoints)
        assertEquals(OverallVerification.UNVERIFIED,VerificationDimensions(EvidenceResult.CONFIRMED,EvidenceResult.UNCONFIRMED).overall)
    }
    @Test fun unknownOrDifferentVersionNeverOpensAppend(){
        for(source in listOf(revision.copy(trust=VersionTrust.UNKNOWN),revision.copy(version="other"),revision.copy(asset="other"),revision.copy(source="other"))){
            val d=Dest(byteArrayOf(1,2,3));assertEquals(GuardedResume.Outcome.REVIEW_REQUIRED,run(d,source).outcome);assertEquals(0,d.opens)
        }
    }
    @Test fun rejectedRangeResponsesNeverOpenAppend(){
        for(m in listOf(ResponseMetadata(200,6,null),ResponseMetadata(416,0,"bytes */6"),ResponseMetadata(206,3,"bytes 0-2/6"),ResponseMetadata(206,3,"bytes 3-5/7"),ResponseMetadata(206,3,"bytes 3-5/6","gzip"))){
            val d=Dest(byteArrayOf(1,2,3));assertEquals(GuardedResume.Outcome.REVIEW_REQUIRED,run(d,metadata=m).outcome);assertEquals(0,d.opens)
        }
    }
    @Test fun corruptedPrefixCannotAppendEvenWithSameLength(){
        val d=Dest(byteArrayOf(1,2,3));val p=PrefixProof(d.inspect(),hash(byteArrayOf(7,8,9)),revision)
        assertEquals(GuardedResume.Outcome.REVIEW_REQUIRED,run(d,proof=p).outcome);assertEquals(0,d.opens)
    }
    @Test fun bindingRaceCannotWrite(){
        val d=Dest(byteArrayOf(1,2,3));d.mutateOnAppend=true
        assertEquals(GuardedResume.Outcome.PARTIAL,run(d).outcome);assertEquals(0,d.opens)
    }
    @Test fun shortAndExcessBodiesNeverComplete(){
        for(tail in listOf(byteArrayOf(4,5),byteArrayOf(4,5,6,7))){
            val d=Dest(byteArrayOf(1,2,3));assertEquals(GuardedResume.Outcome.PARTIAL,run(d,tail=tail).outcome)
            assertArrayEquals(byteArrayOf(1,2,3),d.bytes.take(3).toByteArray())
        }
    }
    @Test fun syncJournalAndCloseFaultsNeverComplete(){
        for(fault in 0..2){val d=Dest(byteArrayOf(1,2,3));d.syncFailure=fault==0;d.closeFailure=fault==2
            assertEquals(GuardedResume.Outcome.PARTIAL,run(d,checkpoint={if(fault==1)throw IOException("fixture")}).outcome)
        }
    }
    @Test fun readBackCorruptionAndCancellationNeverComplete(){
        val d=Dest(byteArrayOf(1,2,3));d.mutateReadBack=true
        assertEquals(GuardedResume.Outcome.PARTIAL,run(d).outcome)
        val cancelled=Dest(byteArrayOf(1,2,3));assertEquals(GuardedResume.Outcome.REVIEW_REQUIRED,run(cancelled,cancelled={true}).outcome)
        assertEquals(0,cancelled.opens)
    }
}
