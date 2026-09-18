package dev.konraditurbe.osmosis.backup

import org.junit.Assert.*
import org.junit.Test

/** Pure policy coverage for the conditions the Android SAF bridge is required to enforce. */
class PhoneToExternalReplicaPolicyTest {
    @Test fun onlyVerifiedPhoneReceiptsCanEnterReplicationQueue(){
        val runtime=AutonomousBackupRuntime()
        val unverified=ReplicaStatus(ReplicaState.COPIED_UNVERIFIED)
        val verified=ReplicaStatus(ReplicaState.VERIFIED,ReplicaProof(2,"a".repeat(64)))
        val (_, work)=runtime.replication(8,mapOf("one" to unverified,"two" to verified),true,false)
        assertEquals(setOf("two"),work)
    }
    @Test fun copiedButNotFinalizedEvidenceCannotCountAsRedundancy(){
        val proof=ReplicaProof(1,"b".repeat(64))
        assertFalse(BackupCompletion.redundancyComplete(listOf(ReplicaStatus(ReplicaState.VERIFIED,proof)),
            listOf(ReplicaStatus(ReplicaState.COPIED_UNVERIFIED,proof)),true,false))
    }
}
