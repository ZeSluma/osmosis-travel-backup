package dev.konraditurbe.osmosis.backup

import org.junit.Assert.*
import org.junit.Test

class ExternalReplicaRecoveryPolicyTest {
    @Test fun unresolvedOperationRefusesDuplicateAllocation(){
        listOf("INTENT","COPYING","PARTIAL","CORRUPT","COMPLETE_UNFINALIZED","UNAVAILABLE").forEach { assertFalse(ExternalReplicaRecoveryPolicy.mayAllocate(listOf(it))) }
    }
    @Test fun onlyVerifiedOrNoOperationAllowsAPlannedAllocation(){
        assertTrue(ExternalReplicaRecoveryPolicy.mayAllocate(emptyList()))
        assertTrue(ExternalReplicaRecoveryPolicy.mayAllocate(listOf("VERIFIED")))
    }
    @Test fun onlyConfirmedMissingCanReopenAllocationAfterReconciliation(){
        assertTrue(ExternalReplicaRecoveryPolicy.mayAllocate(listOf("MISSING")))
        val proof=ReplicaProof(3,"a".repeat(64))
        assertEquals(StagedReplicaObservation.PARTIAL,ExternalReplicaReconciliation.classify(proof,2,"a".repeat(64)))
        assertEquals(StagedReplicaObservation.COMPLETE_UNFINALIZED,ExternalReplicaReconciliation.classify(proof,3,"a".repeat(64)))
        assertEquals(StagedReplicaObservation.CORRUPT,ExternalReplicaReconciliation.classify(proof,3,"b".repeat(64)))
    }
}
