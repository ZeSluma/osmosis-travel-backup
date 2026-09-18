package dev.konraditurbe.osmosis.backup

import org.junit.Assert.*
import org.junit.Test

class ExternalReplicaRecoveryPolicyTest {
    @Test fun unresolvedOperationRefusesDuplicateAllocation(){
        listOf("INTENT","COPYING","PARTIAL").forEach { assertFalse(ExternalReplicaRecoveryPolicy.mayAllocate(listOf(it))) }
    }
    @Test fun onlyVerifiedOrNoOperationAllowsAPlannedAllocation(){
        assertTrue(ExternalReplicaRecoveryPolicy.mayAllocate(emptyList()))
        assertTrue(ExternalReplicaRecoveryPolicy.mayAllocate(listOf("VERIFIED")))
    }
}
