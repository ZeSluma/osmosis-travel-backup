package dev.konraditurbe.osmosis.ledger

import org.junit.Assert.*
import org.junit.Test
import java.time.*

class LedgerModelTest {
    private val now = Instant.parse("2026-09-17T18:00:00Z")
    private fun resolve(vararg times: CaptureCandidate) = CaptureTimeResolver.resolve(times.toList(), now, ZoneId.of("Europe/Berlin"))
    @Test fun captureHasPriorityOverRemoteAndFilename() {
        val result = resolve(CaptureCandidate(TimeSource.REMOTE_FILE, local=LocalDateTime.parse("2026-01-02T12:00:00"), trusted=true),
            CaptureCandidate(TimeSource.CAMERA_CAPTURE, local=LocalDateTime.parse("2026-01-01T12:00:00"), trusted=true))
        assertEquals("2026-01-01", result.day); assertTrue(result.disagreement)
    }
    @Test fun unverifiedFilenameIsNotAuthoritative() {
        assertEquals("SYNC_TIME_FALLBACK", resolve(CaptureCandidate(TimeSource.VERIFIED_FILENAME, local=LocalDateTime.parse("2000-01-01T00:00:00"))).fallback)
    }
    @Test fun secondaryLocalDayBeatsUncertainUtcDate() {
        val r=resolve(CaptureCandidate(TimeSource.CAMERA_CAPTURE,instant=Instant.parse("2026-01-01T23:30:00Z"),trusted=true),
            CaptureCandidate(TimeSource.REMOTE_FILE,local=LocalDateTime.parse("2026-01-02T08:30:00"),trusted=true))
        assertEquals("2026-01-02",r.day);assertEquals("SECONDARY_LOCAL_DAY",r.fallback);assertTrue(r.disagreement)
    }
    @Test fun literalDateWithoutZoneIsPreserved() {
        val result=resolve(CaptureCandidate(TimeSource.CAMERA_CAPTURE, local=LocalDateTime.parse("2026-11-15T23:59:59"),trusted=true))
        assertEquals("2026-11-15",result.day); assertEquals("CAMERA_LOCAL_ZONE_UNKNOWN",result.fallback)
    }
    @Test fun instantWithoutZoneUsesExplicitUtc() {
        val result=resolve(CaptureCandidate(TimeSource.REMOTE_FILE,instant=Instant.parse("2026-01-01T23:59:00Z"),trusted=true))
        assertEquals("2026-01-01",result.day); assertEquals("UTC_DAY_FALLBACK",result.fallback)
    }
    @Test fun offsetPreservesMidnightAcrossTravel() {
        val result=resolve(CaptureCandidate(TimeSource.CAMERA_CAPTURE,instant=Instant.parse("2026-11-15T23:30:00Z"),offset=ZoneOffset.ofHours(9),trusted=true))
        assertEquals("2026-11-16",result.day)
    }
    @Test fun delayedSyncUsesOriginalDay() {
        assertEquals("2020-02-29",resolve(CaptureCandidate(TimeSource.CAMERA_CAPTURE,local=LocalDateTime.parse("2020-02-29T08:00:00"),trusted=true)).day)
    }
    @Test fun dstGapIsConflictNotSilentNormalization() {
        assertEquals("CONFLICT",resolve(CaptureCandidate(TimeSource.CAMERA_CAPTURE,local=LocalDateTime.parse("2026-03-29T02:30:00"),zone=ZoneId.of("Europe/Berlin"),trusted=true)).confidence)
    }
    @Test fun dstOverlapDoesNotInventInstant() {
        val result=resolve(CaptureCandidate(TimeSource.CAMERA_CAPTURE,local=LocalDateTime.parse("2026-10-25T02:30:00"),trusted=true))
        assertEquals("2026-10-25",result.day); assertNull(result.zone)
    }
    @Test fun fallbackRecordsFirstZoneAndDay() { val r=resolve(); assertEquals("2026-09-17",r.day); assertEquals("Europe/Berlin",r.zone); assertEquals("UNCERTAIN",r.confidence) }
    @Test fun filenamesAloneNeverEstablishIdentity() {
        val a=RemoteAsset("0","DCIM/A.MP4",10)
        assertNotEquals(a.identity("a"),a.identity("b")); assertNotEquals(a.identity("a"),a.copy(storage="1").identity("a"))
        assertNotEquals(a.identity("a"),a.copy(size=11).identity("a")); assertNotEquals(a.identity("a"),a.copy(remoteTime="changed").identity("a"))
    }
    @Test fun fingerprintIsUnambiguousLengthDelimited() { assertNotEquals(key("ab","c"),key("a","bc")) }
    @Test fun sameMetadataStillRequiresRevalidation() { assertEquals(PlanAction.REVALIDATE_IDENTITY,SyncPlanner.action(AssetClass.KNOWN_REQUIRED,TransferState.DISCOVERED,true)) }
    @Test fun rememberedVerifiedDoesNotProveAnything() { assertEquals(PlanAction.REVALIDATE_IDENTITY,SyncPlanner.action(AssetClass.KNOWN_REQUIRED,TransferState.LOCAL_VERIFIED,false)) }
    @Test fun genuinelySatisfiedAssetNotRetransferred() { assertNull(SyncPlanner.action(AssetClass.KNOWN_REQUIRED,TransferState.LOCAL_VERIFIED,false,true)) }
    @Test fun ambiguousIdentityOverridesProof() { assertNotNull(SyncPlanner.action(AssetClass.KNOWN_REQUIRED,TransferState.LOCAL_VERIFIED,true,true)) }
    @Test fun partialRequiresResumeValidation() { assertEquals(PlanAction.RESUME_REVALIDATE,SyncPlanner.action(AssetClass.KNOWN_REQUIRED,TransferState.PARTIAL,true)) }
    @Test fun successfulTransferStillRequiresVerification() { assertEquals(PlanAction.VERIFY_EXISTING,SyncPlanner.action(AssetClass.KNOWN_REQUIRED,TransferState.TRANSFERRED_UNVERIFIED,true)) }
    @Test fun unknownsNeverDisappear() { assertEquals(PlanAction.REVIEW_UNKNOWN,SyncPlanner.action(AssetClass.UNKNOWN_POTENTIALLY_REQUIRED,TransferState.DISCOVERED,false)) }
    @Test fun unsupportedRequiresReview() { assertEquals(PlanAction.REVIEW_UNKNOWN,SyncPlanner.action(AssetClass.UNSUPPORTED,TransferState.DISCOVERED,false)) }
    @Test fun provenRegenerableExcluded() { assertNull(SyncPlanner.action(AssetClass.KNOWN_REGENERABLE_EXCLUDED,TransferState.DISCOVERED,false)) }
    @Test fun evidencedUnrelatedUnknownDoesNotBlockRecording() { assertNull(SyncPlanner.action(AssetClass.UNKNOWN_NON_RECORDING,TransferState.DISCOVERED,false)) }
    @Test fun knownOptionalNotRequiredByDefault() { assertNull(SyncPlanner.action(AssetClass.KNOWN_OPTIONAL,TransferState.DISCOVERED,false)) }
    @Test fun newRequiredSchedulesDownload() { assertEquals(PlanAction.DOWNLOAD,SyncPlanner.action(AssetClass.KNOWN_REQUIRED,TransferState.DISCOVERED,false)) }
    @Test fun newAbsentRequiredAssetMayDownloadWithoutImmutableVersion() {
        assertEquals(PlanAction.DOWNLOAD,SyncPlanner.action(AssetClass.KNOWN_REQUIRED,TransferState.DISCOVERED,true,
            localPresence=LocalPresence.ABSENT))
    }
    @Test fun failureSchedulesRetry() { assertEquals(PlanAction.DOWNLOAD,SyncPlanner.action(AssetClass.KNOWN_REQUIRED,TransferState.FAILED,false)) }
    @Test fun twoDaysUseDistinctDirectories() { assertNotEquals(SyncPlanner.relativePath("2026-01-01","a.mp4","a"),SyncPlanner.relativePath("2026-01-02","a.mp4","a")) }
    @Test fun sameNameDistinctAssetCannotOverwrite() { assertNotEquals(SyncPlanner.relativePath("2026-01-01","a.mp4",key("a")),SyncPlanner.relativePath("2026-01-01","a.mp4",key("b"))) }
    @Test fun pathCannotEscapeRoot() { val p=SyncPlanner.relativePath("2026-01-01","../../x\\../../bad?.mp4",key("a")); assertEquals(1,p.count { it=='/' }); assertFalse(p.contains("..")); assertFalse(p.contains('?')) }
    @Test fun repeatPathDeterministic() { assertEquals(SyncPlanner.relativePath("2026-01-01","a.mp4",key("a")),SyncPlanner.relativePath("2026-01-01","a.mp4",key("a"))) }
    @Test(expected=IllegalArgumentException::class) fun negativeSizeRejected() { RemoteAsset("0","a",-1) }
    @Test(expected=IllegalArgumentException::class) fun fabricatedGroupingRejected() { RemoteAsset("0","a",1,relationshipProven=true) }
}
