package dev.konraditurbe.osmosis.backup

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalReplicaRefreshPolicyTest {
    @Test fun terminalRefreshIsReadOnlyButLaterRefreshMayScheduleAgain() {
        assertFalse(ExternalReplicaRefreshPolicy.shouldStart(true))
        assertTrue(ExternalReplicaRefreshPolicy.shouldStart(false))
    }
}
