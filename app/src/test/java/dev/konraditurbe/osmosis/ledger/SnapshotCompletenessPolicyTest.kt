package dev.konraditurbe.osmosis.ledger

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapshotCompletenessPolicyTest {
    @Test fun historicalIdentityAmbiguityMayNotBlockSafeCurrentNewWork() {
        assertTrue(SnapshotCompletenessPolicy.currentInventoryEligible(
            "pages=true;stores=true;members=true;stable=true", "IDENTITY_UNRESOLVED", 0))
    }
    @Test fun currentAmbiguityOrCoverageFailureBlocksAutomaticDownload() {
        assertFalse(SnapshotCompletenessPolicy.currentInventoryEligible(
            "pages=true;stores=true;members=true;stable=true", "IDENTITY_UNRESOLVED", 1))
        assertFalse(SnapshotCompletenessPolicy.currentInventoryEligible(
            "pages=true;stores=false;members=true;stable=true", null, 0))
    }
}
