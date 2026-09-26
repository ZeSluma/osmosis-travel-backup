package dev.konraditurbe.osmosis.ledger

import dev.konraditurbe.osmosis.backup.SafReplicaPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Deterministic CD01-CD08 policy coverage. Pocket timestamp/type semantics and provider behavior
 * intentionally remain outside this pure matrix.
 */
class CaptureDayFaultMatrixTest {
    private val allocatedAt = Instant.parse("2026-12-31T23:30:00Z")

    private fun resolve(zone: String = "Europe/Berlin", vararg candidates: CaptureCandidate) =
        CaptureTimeResolver.resolve(candidates.toList(), allocatedAt, ZoneId.of(zone))

    @Test fun sameDayPathsAreStableDistinctAndPortableAcrossReplicaRoots() {
        val first = SyncPlanner.relativePath("2026-01-01", "DCIM/DJI_20260101120000_0001_D.MP4", "asset-a")
        val sameAgain = SyncPlanner.relativePath("2026-01-01", "DCIM/DJI_20260101120000_0001_D.MP4", "asset-a")
        val collision = SyncPlanner.relativePath("2026-01-01", "DCIM/DJI_20260101120000_0001_D.MP4", "asset-b")
        assertEquals(first, sameAgain) // CD01/CD04: retry/restart keeps a frozen logical path.
        assertNotEquals(first, collision) // CD01/CD08: a same-name asset cannot overwrite it.
        val replicaPath = SafReplicaPath.parse(first)
        assertEquals("2026-01-01", replicaPath.captureDay)
        assertEquals(first.substringAfter('/'), replicaPath.fileName) // CD08: the same logical mapping is valid for a new root.
    }

    @Test fun captureLocalDayWinsAcrossDelayedSyncTravelAndMidnightBoundaries() {
        val late = resolve("Pacific/Honolulu", CaptureCandidate(
            TimeSource.CAMERA_CAPTURE, local = LocalDateTime.parse("2026-01-01T23:59:59"), trusted = true))
        val next = resolve("Pacific/Kiritimati", CaptureCandidate(
            TimeSource.CAMERA_CAPTURE, local = LocalDateTime.parse("2026-01-02T00:00:01"), trusted = true))
        val offset = resolve("Pacific/Honolulu", CaptureCandidate(
            TimeSource.CAMERA_CAPTURE, instant = Instant.parse("2026-01-01T15:30:00Z"), offset = ZoneOffset.ofHours(9), trusted = true))
        assertEquals("2026-01-01", late.day) // CD02/CD03: no download-day or phone-zone rewrite.
        assertEquals("2026-01-02", next.day)
        assertEquals("2026-01-02", offset.day) // CD06: source offset decides the capture-local day.
    }

    @Test fun companionGroupingNeedsExplicitEvidenceRatherThanFilenameSimilarity() {
        val unproven = RemoteAsset("camera", "DCIM/DJI_20260101120000_0001_D.WAV", 10)
        assertFalse(unproven.relationshipProven)
        assertNull(unproven.recordingKey)
        assertTrue(runCatching {
            RemoteAsset("camera", "DCIM/DJI_20260101120000_0001_D.WAV", 10,
                relationshipProven = true)
        }.isFailure) // CD05: names alone cannot attach a sidecar to a parent group.
    }

    @Test fun unknownOrConflictingTimeRemainsExplicitlyUncertain() {
        val fallback = resolve("Europe/Berlin")
        val conflict = resolve("Europe/Berlin",
            CaptureCandidate(TimeSource.CAMERA_CAPTURE, local = LocalDateTime.parse("2026-03-29T02:30:00"),
                zone = ZoneId.of("Europe/Berlin"), trusted = true),
            CaptureCandidate(TimeSource.REMOTE_FILE, local = LocalDateTime.parse("2026-03-30T12:00:00"), trusted = true))
        assertEquals("SYNC_TIME_FALLBACK", fallback.fallback)
        assertEquals("UNCERTAIN", fallback.confidence)
        assertTrue(conflict.disagreement)
        assertEquals("CONFLICT", conflict.confidence) // CD07: no silent normalization.
    }
}
