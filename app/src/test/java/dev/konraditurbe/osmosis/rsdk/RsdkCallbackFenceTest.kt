package dev.konraditurbe.osmosis.rsdk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RsdkCallbackFenceTest {
    @Test fun replacementRejectsCallbacksFromTheReleasedClient() {
        val fence = RsdkCallbackFence()
        val old = fence.begin()
        val replacement = fence.begin()

        assertFalse(fence.accepts(old))
        assertTrue(fence.accepts(replacement))
    }

    @Test fun explicitReleaseRejectsItsQueuedCallbacks() {
        val fence = RsdkCallbackFence()
        val active = fence.begin()
        fence.invalidate()

        assertFalse(fence.accepts(active))
    }
}
