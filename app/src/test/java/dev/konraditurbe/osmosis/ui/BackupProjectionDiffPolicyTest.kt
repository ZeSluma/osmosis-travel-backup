package dev.konraditurbe.osmosis.ui

import dev.konraditurbe.osmosis.connection.LiveTransferFileProjection
import dev.konraditurbe.osmosis.ledger.BackupDisplay
import dev.konraditurbe.osmosis.ledger.BackupDisplayState
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupProjectionDiffPolicyTest {
    @Test fun unchangedServicePublishDoesNotRepaintAnyVideo() {
        val durable = mapOf("one" to BackupDisplay(BackupDisplayState.NEW))
        val live = mapOf("one" to LiveTransferFileProjection(LiveTransferFileProjection.Phase.DOWNLOADING, 12))

        assertEquals(emptySet<String>(), BackupProjectionDiffPolicy.changedKeys(durable, durable, live, live))
    }

    @Test fun progressTickRepaintsOnlyTheActiveVideo() {
        val previous = mapOf("one" to LiveTransferFileProjection(LiveTransferFileProjection.Phase.DOWNLOADING, 12))
        val next = mapOf("one" to LiveTransferFileProjection(LiveTransferFileProjection.Phase.DOWNLOADING, 13))

        assertEquals(setOf("one"), BackupProjectionDiffPolicy.changedKeys(
            mapOf("one" to BackupDisplay(BackupDisplayState.NEW)),
            mapOf("one" to BackupDisplay(BackupDisplayState.NEW)), previous, next))
    }

    @Test fun terminalOverlayRemovalRepaintsOnlyItsFormerOwner() {
        assertEquals(setOf("one"), BackupProjectionDiffPolicy.changedKeys(
            mapOf("one" to BackupDisplay(BackupDisplayState.NEW)),
            mapOf("one" to BackupDisplay(BackupDisplayState.LOCAL_INTEGRITY_CONFIRMED)),
            mapOf("one" to LiveTransferFileProjection(LiveTransferFileProjection.Phase.DOWNLOADING, 100)), emptyMap()))
    }
}
