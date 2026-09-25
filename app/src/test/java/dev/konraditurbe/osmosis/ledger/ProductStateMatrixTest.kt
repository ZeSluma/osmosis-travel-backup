package dev.konraditurbe.osmosis.ledger

import dev.konraditurbe.osmosis.backup.BackupStatusProjection
import dev.konraditurbe.osmosis.backup.ReplicaState
import dev.konraditurbe.osmosis.backup.ReplicaStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Every visible backup label must be grounded in one unambiguous durable evidence combination. */
class ProductStateMatrixTest {
    @Test fun visibleAssetStatesAreCompleteAndFailClosed() {
        fun state(transfer: String, presence: String, locator: Boolean, bytes: Long, expected: Long?, published: Boolean = false) =
            BackupDisplayPolicy.resolve(transfer, presence, locator, bytes, expected, published).state
        assertEquals(BackupDisplayState.NEW, state("DISCOVERED", "ABSENT", false, 0, 100))
        assertEquals(BackupDisplayState.PARTIAL_REVIEW, state("PARTIAL", "PRESENT_UNVERIFIED", true, 20, 100))
        assertEquals(BackupDisplayState.LOCAL_INTEGRITY_CONFIRMED,
            state("TRANSFERRED_UNVERIFIED", "PRESENT_UNVERIFIED", true, 100, 100, true))
        assertEquals(BackupDisplayState.EXISTING_UNVERIFIED,
            state("LOCAL_PRESENT_UNVERIFIED", "PRESENT_UNVERIFIED", true, 0, 100))
        assertEquals(BackupDisplayState.REVIEW_REQUIRED, state("LOCAL_VERIFIED", "AMBIGUOUS", true, 100, 100, true))
        assertEquals(BackupDisplayState.REVIEW_REQUIRED, state("TRANSFERRED_UNVERIFIED", "PRESENT_UNVERIFIED", true, 100, 100, false))
    }

    @Test fun localIntegrityDoesNotPromoteCameraSyncOrRedundancyWithOpenSourceIdentity() {
        val localProof = ReplicaStatus(ReplicaState.VERIFIED)
        val status = BackupStatusProjection.derive(
            inventoryTrusted = true,
            unknownRequired = true,
            phone = listOf(localProof),
            external = listOf(localProof),
            freshSourceRevalidation = true,
        )
        assertFalse(status.cameraSyncComplete)
        assertFalse(status.redundancyComplete)
        assertFalse(status.safeToClearCamera)
    }

    @Test fun visibleProductStateMatrixRequiresEveryDurableCompletionPrerequisite() {
        val verified = ReplicaStatus(ReplicaState.VERIFIED)
        val missing = ReplicaStatus(ReplicaState.NOT_PRESENT)

        val complete = BackupStatusProjection.derive(true, false, listOf(verified), listOf(verified), true)
        assertTrue(complete.inventoryTrusted)
        assertTrue(complete.cameraSyncComplete)
        assertTrue(complete.redundancyComplete)
        assertTrue(complete.safeToClearCamera)

        val incompleteInventory = BackupStatusProjection.derive(false, false, listOf(verified), listOf(verified), true)
        assertFalse(incompleteInventory.cameraSyncComplete)
        assertFalse(incompleteInventory.redundancyComplete)
        assertFalse(incompleteInventory.safeToClearCamera)

        val missingSsd = BackupStatusProjection.derive(true, false, listOf(verified), listOf(missing), true)
        assertTrue(missingSsd.cameraSyncComplete)
        assertFalse(missingSsd.redundancyComplete)
        assertFalse(missingSsd.safeToClearCamera)

        val staleSource = BackupStatusProjection.derive(true, false, listOf(verified), listOf(verified), false)
        assertTrue(staleSource.cameraSyncComplete)
        assertTrue(staleSource.redundancyComplete)
        assertFalse(staleSource.safeToClearCamera)
    }
}
