package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Test

class LiveTransferFileProjectionPolicyTest {
    @Test fun fileProgressIsBoundedAndTerminalStatesDoNotPretendToDownload() {
        assertEquals(LiveTransferFileProjection(LiveTransferFileProjection.Phase.DOWNLOADING, 0),
            LiveTransferFileProjectionPolicy.downloading(-1, 100))
        assertEquals(LiveTransferFileProjection(LiveTransferFileProjection.Phase.DOWNLOADING, 100),
            LiveTransferFileProjectionPolicy.downloading(200, 100))
        assertEquals(LiveTransferFileProjection(LiveTransferFileProjection.Phase.INTEGRITY_SAVED),
            LiveTransferFileProjectionPolicy.completed(true))
        assertEquals(LiveTransferFileProjection(LiveTransferFileProjection.Phase.REVIEW_REQUIRED),
            LiveTransferFileProjectionPolicy.completed(false))
    }

    @Test fun terminalDurableRereadRemovesEveryTransientCellOverlay() {
        val active = LiveTransferFileProjectionPolicy.update(emptyMap(), "asset-a",
            LiveTransferFileProjectionPolicy.downloading(30, 100))
        assertEquals(1, active.size)
        assertEquals(emptyMap<String, LiveTransferFileProjection>(),
            LiveTransferFileProjectionPolicy.clearAtTerminal())
    }
}
