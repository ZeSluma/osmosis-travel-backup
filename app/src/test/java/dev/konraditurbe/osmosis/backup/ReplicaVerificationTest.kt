package dev.konraditurbe.osmosis.backup

import org.junit.Assert.*
import org.junit.Test
import java.io.*

class ReplicaVerificationTest {
    private val source = ByteArray(140_000) { (it % 251).toByte() }
    private val proof = ReplicaProof(source.size.toLong(), ReplicaVerification.hex(java.security.MessageDigest.getInstance("SHA-256").digest(source)))

    private class FakePending(private val corruptReadback:Boolean=false, private val failAfter:Int?=null):PendingReplica {
        private val bytes=ByteArrayOutputStream(); var final=false; var syncs=0
        override val locator="fake:pending"
        override fun output()=object:OutputStream(){override fun write(b:Int){ bytes.write(b) }
            override fun write(b:ByteArray,off:Int,len:Int){if(failAfter!=null && bytes.size()+len>failAfter!!)throw IOException();bytes.write(b,off,len)}}
        override fun input():InputStream { val v=bytes.toByteArray();if(corruptReadback && v.isNotEmpty())v[0]=(v[0]+1).toByte();return v.inputStream() }
        override fun sync(){syncs++};override fun bytes()=bytes.size().toLong();override fun finalizeReplica(){final=true}
    }

    @Test fun verifiedReplicaRequiresIndependentReadbackBeforeFinalization(){
        val pending=FakePending(); val result=ReplicaVerification.copy(proof,{source.inputStream()},pending)
        assertTrue(result is ReplicaVerification.Result.Verified);assertTrue(pending.final);assertTrue(pending.syncs>0)
    }
    @Test fun corruptedReplicaIsNeverFinalized(){
        val pending=FakePending(corruptReadback=true); val result=ReplicaVerification.copy(proof,{source.inputStream()},pending)
        assertEquals("DESTINATION_CHECKSUM_MISMATCH",(result as ReplicaVerification.Result.Rejected).reason);assertFalse(pending.final)
    }
    @Test fun truncatedOrInterruptedCopyRemainsIncomplete(){
        val pending=FakePending(failAfter=70_000); val result=ReplicaVerification.copy(proof,{source.inputStream()},pending)
        assertTrue(result is ReplicaVerification.Result.Incomplete);assertFalse(pending.final)
        val stopped=FakePending(); val cancelled=ReplicaVerification.copy(proof,{source.inputStream()},stopped,{stopped.bytes()>0})
        assertTrue(cancelled is ReplicaVerification.Result.Incomplete);assertFalse(stopped.final)
    }
    @Test fun completenessRequiresTrustedInventoryAndBothIndependentDomains(){
        val ok=ReplicaStatus(ReplicaState.VERIFIED,proof); val missing=ReplicaStatus(ReplicaState.NOT_PRESENT)
        assertTrue(BackupCompletion.cameraSyncComplete(listOf(ok),true,false))
        assertFalse(BackupCompletion.cameraSyncComplete(listOf(ok),false,false))
        assertTrue(BackupCompletion.redundancyComplete(listOf(ok),listOf(ok),true,false))
        assertFalse(BackupCompletion.redundancyComplete(listOf(ok),listOf(missing),true,false))
        assertFalse(BackupCompletion.safeToClearCamera(true,false,false))
    }
}
