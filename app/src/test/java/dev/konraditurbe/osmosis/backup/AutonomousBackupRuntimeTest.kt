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
    @Test fun trustedCurrentDownloadMayRunWhileHistoricalCompletenessStaysFalse(){
        val currentSafe = complete.copy(enumerationComplete = false, recordingComplete = false,
            localComplete = false, automaticDownloadEligible = true)
        val (state, work) = AutonomousBackupRuntime().plan(3, SourceTrust.TRUSTED, currentSafe, false)
        assertEquals(BackupPhase.CAMERA_TRANSFER, state.phase)
        assertEquals(setOf("new"), work)
        assertFalse(currentSafe.enumerationComplete)
    }
    @Test fun staleCameraResultCannotCompleteReplacement(){
        val runtime=AutonomousBackupRuntime();val old=runtime.plan(1,SourceTrust.TRUSTED,complete,false).first.lease!!
        val replacement=runtime.plan(2,SourceTrust.TRUSTED,complete,false).first.lease!!
        assertFalse(runtime.complete(old));assertTrue(runtime.accepts(replacement));assertTrue(runtime.complete(replacement))
    }
    @Test fun repeatedPlannerCallbackCannotAllocateSecondCameraWriterForSameEpoch(){
        val runtime=AutonomousBackupRuntime();val first=runtime.plan(7,SourceTrust.TRUSTED,complete,false)
        val repeated=runtime.plan(7,SourceTrust.TRUSTED,complete,false)
        assertEquals(first.first.lease,repeated.first.lease);assertTrue(repeated.second.isEmpty())
        assertTrue(runtime.accepts(checkNotNull(first.first.lease)))
    }
    @Test fun failedLeaseCannotCompleteButFreshTrustedPlanGetsReplacementWriter(){
        val runtime=AutonomousBackupRuntime();val failed=runtime.plan(9,SourceTrust.TRUSTED,complete,false).first.lease!!
        assertTrue(runtime.fail(failed,"TRANSFER_REVIEW_REQUIRED"));assertFalse(runtime.complete(failed))
        val replacement=runtime.plan(10,SourceTrust.TRUSTED,complete,false).first.lease!!
        assertFalse(runtime.accepts(failed));assertTrue(runtime.accepts(replacement));assertNotEquals(failed,replacement)
    }
    @Test fun ssdWorkRequiresPhoneProofAndAvailabilityAndUserStopWins(){
        val runtime=AutonomousBackupRuntime();val good=ReplicaStatus(ReplicaState.VERIFIED,ReplicaProof(1,"a".repeat(64)))
        assertTrue(runtime.replication(4,mapOf("a" to good),false,false).second.isEmpty())
        val (state,work)=runtime.replication(4,mapOf("a" to good,"b" to ReplicaStatus(ReplicaState.PARTIAL)),true,false)
        assertEquals(BackupPhase.REPLICATION,state.phase);assertEquals(setOf("a"),work)
        assertTrue(runtime.replication(4,mapOf("a" to good),true,true).second.isEmpty())
        assertEquals(BackupPhase.STOPPED,runtime.snapshot().phase)
    }
    @Test fun repeatedReplicaSchedulerCallbackCannotAllocateSecondWriterForSameEpoch(){
        val runtime=AutonomousBackupRuntime();val good=ReplicaStatus(ReplicaState.VERIFIED,ReplicaProof(1,"a".repeat(64)))
        val first=runtime.replication(8,mapOf("a" to good),true,false)
        val repeated=runtime.replication(8,mapOf("a" to good),true,false)
        assertEquals(first.first.lease,repeated.first.lease);assertTrue(repeated.second.isEmpty())
    }
}
