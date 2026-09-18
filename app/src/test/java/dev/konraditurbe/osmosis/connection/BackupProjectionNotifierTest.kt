package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupProjectionNotifierTest {
    @Test fun `removed UI observer receives no later service projection`() {
        val notifier = BackupProjectionNotifier()
        var first = 0
        var second = 0
        val removeFirst = notifier.observe { first++ }
        notifier.observe { second++ }
        notifier.publish()
        removeFirst()
        notifier.publish()
        assertEquals(1, first)
        assertEquals(2, second)
    }
}
