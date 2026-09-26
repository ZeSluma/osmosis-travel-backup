package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupProjectionNotifierTest {
    @Test fun `observer receives current durable projection immediately and stops after removal`() {
        val notifier = BackupProjectionNotifier()
        var first = 0
        var second = 0
        val removeFirst = notifier.observe { first++ }
        notifier.observe { second++ }
        assertEquals(1, first)
        assertEquals(1, second)
        notifier.publish()
        removeFirst()
        notifier.publish()
        assertEquals(2, first)
        assertEquals(3, second)
    }

    @Test fun `observer added after a background transition is immediately invalidated`() {
        val notifier = BackupProjectionNotifier()
        notifier.publish() // Service work may complete while no Activity observes it.
        var reads = 0
        notifier.observe { reads++ }
        assertEquals(1, reads)
    }
}
