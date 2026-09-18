package dev.konraditurbe.osmosis.backup

import org.junit.Assert.*
import org.junit.Test

class SafReplicaPathTest {
    @Test fun captureDayPathIsPreservedAndStrictlyParsed(){
        assertEquals(SafReplicaPath("2026-09-18","DJI_20260918100000_0001_D.MP4"),
            SafReplicaPath.parse("2026-09-18/DJI_20260918100000_0001_D.MP4"))
    }
    @Test fun unsafeOrInvalidCalendarPathIsRefused(){
        listOf("../a.mp4","2026-99-99/a.mp4","2026-09-18/a/b.mp4","2026-09-18/a file.mp4").forEach {
            assertTrue(runCatching { SafReplicaPath.parse(it) }.isFailure)
        }
    }
}
