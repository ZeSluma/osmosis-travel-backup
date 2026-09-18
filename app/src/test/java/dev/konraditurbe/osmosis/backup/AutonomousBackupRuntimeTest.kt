package dev.konraditurbe.osmosis.backup

import dev.konraditurbe.osmosis.connection.SourceTrust
import dev.konraditurbe.osmosis.ledger.*
import org.junit.Assert.*
import org.junit.Test

class AutonomousBackupRuntimeTest {
    private val complete=PlanResult("s",listOf(PlanItem("new",PlanAction.DOWNLOAD,"2026-09-18/a.mp4")),true,true,true)
    @Test fun onlyTrustedCompleteInventoryAutoStartsCameraWork(){
        val runtime=AutonomousBackupRuntime()
        assertTrue(runtime.plan(3,SourceTrust.INCOMPLETE_UNTRUSTED,complete,false).second.isEmpty())
        val (state,work)=runtime.plan(3,SourceTrust.TRUSTED,complete,false)
        assertEquals(BackupPhase.CAMERA_TRANSFER,state.phase);assertEquals(setOf("new"),work)
    }
    @Test fun staleCameraResultCannotCompleteReplacement(){
        val runtime=AutonomousBackupRuntime();val old=runtime.plan(1,SourceTrust.TRUSTED,complete,false).first.lease!!
        val replacement=runtime.plan(2,SourceTrust.TRUSTED,complete,false).first.lease!!
        assertFalse(runtime.complete(old));assertTrue(runtime.accepts(replacement));assertTrue(runtime.complete(replacement))
    }
    @Test fun ssdWorkRequiresPhoneProofAndAvailabilityAndUserStopWins(){
        val runtime=AutonomousBackupRuntime();val good=ReplicaStatus(ReplicaState.VERIFIED,ReplicaProof(1,"a".repeat(64)))
        assertTrue(runtime.replication(4,mapOf("a" to good),false,false).second.isEmpty())
        val (state,work)=runtime.replication(4,mapOf("a" to good,"b" to ReplicaStatus(ReplicaState.PARTIAL)),true,false)
        assertEquals(BackupPhase.REPLICATION,state.phase);assertEquals(setOf("a"),work)
        assertTrue(runtime.replication(4,mapOf("a" to good),true,true).second.isEmpty())
        assertEquals(BackupPhase.STOPPED,runtime.snapshot().phase)
    }
}
