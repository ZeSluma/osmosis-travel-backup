package dev.konraditurbe.osmosis.ledger

import org.junit.Assert.*
import org.junit.Test
import java.time.*

class DjiFilenameTimeTest {
    private val name = "DJI_20260917184037_0003_D.MP4"
    private fun resolve(name: String, zone: String = "Europe/Berlin", vararg stronger: CaptureCandidate) =
        CaptureTimeResolver.resolve(listOfNotNull(DjiFilenameTime.parseFilename(name)) + stronger,
            Instant.parse("2026-09-20T23:59:00Z"), ZoneId.of(zone))
    @Test fun exactNameHasLocalTimeOnly() {
        val c = DjiFilenameTime.parseFilename(name)!!
        assertEquals(LocalDateTime.parse("2026-09-17T18:40:37"), c.local)
        assertNull(c.instant); assertNull(c.zone); assertNull(c.offset)
        assertEquals(TimeSource.DJI_FILENAME,c.source)
    }
    @Test fun invalidCalendarAndClockRejected() {
        for (s in listOf("20260230184037","20260229184037","20261317184037","20260917244037","20260917186037","20260917184060","00000917184037"))
            assertNull(DjiFilenameTime.parseFilename("DJI_${s}_0003_D.MP4"))
    }
    @Test fun leapDayAccepted() { assertNotNull(DjiFilenameTime.parseFilename("DJI_20240229235959_0001_D.MP4")) }
    @Test fun malformedAndNonDjiRejected() {
        for (s in listOf("x$name", "$name.tmp", "dji_20260917184037_0003_D.MP4", "DJI_20260917184037_003_D.MP4", "DJI_20260917184037_0003_X.MP4", "DJI_20260917184037_0003_D.LRF", "IMG_20260917184037_0003_D.MP4", "/$name", "$name\n"))
            assertNull(DjiFilenameTime.parseFilename(s))
    }
    @Test fun remoteAssetBasenameIsUsed() { assertEquals(DjiFilenameTime.parseFilename(name), DjiFilenameTime.fromRemotePath("DCIM/100MEDIA/$name")) }
    @Test fun timezoneUnknownAndNotUtc() {
        val r = resolve(name)
        assertEquals("DJI_FILENAME",r.source); assertNull(r.zone)
        assertEquals("2026-09-17T18:40:37",r.timestamp)
        assertEquals("CAMERA_LOCAL_ZONE_UNKNOWN",r.fallback)
        assertEquals("HIGH_LOCAL_DATE_UNKNOWN_INSTANT",r.confidence)
    }
    @Test fun phoneTimezoneCannotMoveCameraDay() {
        assertEquals(resolve(name,"Pacific/Kiritimati"),resolve(name,"Pacific/Honolulu"))
    }
    @Test fun midnightBoundariesStayLocal() {
        assertEquals("2026-09-17",resolve("DJI_20260917235959_0001_D.MP4","Pacific/Kiritimati").day)
        assertEquals("2026-09-18",resolve("DJI_20260918000000_0002_D.MP4","Pacific/Honolulu").day)
    }
    @Test fun filenameBeatsDelayedSyncAndRemoteMtime() {
        val r=resolve(name,"Europe/Berlin",CaptureCandidate(TimeSource.REMOTE_FILE,local=LocalDateTime.parse("2026-09-20T10:00:00"),trusted=true))
        assertEquals("DJI_FILENAME",r.source); assertEquals("2026-09-17",r.day); assertTrue(r.disagreement)
        assertEquals("SYNC_FALLBACK",resolve("unrecognized.mp4").source)
    }
    @Test fun explicitCaptureWithoutZoneOverridesFilename() {
        assertEquals("CAMERA_CAPTURE",resolve(name,"Europe/Berlin",CaptureCandidate(TimeSource.CAMERA_CAPTURE,
            local=LocalDateTime.parse("2026-09-17T18:40:36"),trusted=true)).source)
    }
    @Test fun explicitCaptureWithZoneWinsRegardlessOfInputOrder() {
        val local=CaptureCandidate(TimeSource.CAMERA_CAPTURE,local=LocalDateTime.parse("2026-09-17T18:40:36"),trusted=true)
        val offset=local.copy(local=LocalDateTime.parse("2026-09-17T18:40:35"),offset=ZoneOffset.ofHours(9))
        assertEquals("+09:00",resolve(name,"Europe/Berlin",local,offset).zone)
    }
    @Test fun untrustedMetadataDoesNotOverride() {
        assertEquals("DJI_FILENAME",resolve(name,"Europe/Berlin",CaptureCandidate(TimeSource.CAMERA_CAPTURE,
            local=LocalDateTime.parse("2000-01-01T00:00:00"))).source)
    }
}
