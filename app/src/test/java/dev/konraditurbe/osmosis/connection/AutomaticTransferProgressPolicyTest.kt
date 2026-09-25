package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomaticTransferProgressPolicyTest {
    @Test fun projectionContainsOnlyPercentAndCountAndClampsUntrustedCounters() {
        assertEquals("transfer=37% (1/2)", AutomaticTransferProgressPolicy.project(2, 200L, 1, 75L).text)
        assertEquals("transfer=100% (0/1)", AutomaticTransferProgressPolicy.project(1, 100L, 0, 999L).text)
        assertEquals("transfer=0% (0/0)", AutomaticTransferProgressPolicy.project(-1, 0L, 0, -1L).text)
    }

    @Test fun observerIsNotWokenForRepeatedSamePercent() {
        assertTrue(AutomaticTransferProgressPolicy.shouldPublish(-1L, 0L))
        assertFalse(AutomaticTransferProgressPolicy.shouldPublish(37L, 37L))
        assertTrue(AutomaticTransferProgressPolicy.shouldPublish(37L, 38L))
    }
}
