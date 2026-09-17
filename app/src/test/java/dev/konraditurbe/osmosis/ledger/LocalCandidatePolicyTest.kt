package dev.konraditurbe.osmosis.ledger

import org.junit.Assert.*
import org.junit.Test

class LocalCandidatePolicyTest {
    private val local = LocalMediaObservation("content://media/external_primary/file/7", "original.mp4",
        "Movies/Osmosis/", 3_071_380_142, false, "version1-generation2")
    private fun assess(items: List<LocalMediaObservation> = listOf(local), prior: List<LocalMatch> = emptyList(), complete: Boolean = true) =
        LocalCandidatePolicy.assess("DCIM/original.mp4", local.bytes, "2026-09-17/reserved.mp4", LocalInventory(items, complete), prior)
    private fun action(p: LocalPresence, s: TransferState = TransferState.DISCOVERED) =
        SyncPlanner.action(AssetClass.KNOWN_REQUIRED,s,true,localPresence=p)

    @Test fun emptyLedgerFindsPreLedgerLegacyFile() {
        val r=assess(); assertEquals(LocalPresence.PRESENT_UNVERIFIED,r.presence)
        assertEquals("LEGACY_NAME_SIZE_SCOPE_PUBLISHED",r.matches.single().evidence)
        assertEquals(local.locator,r.matches.single().media.locator)
    }
    @Test fun multiGigabyteCandidateIsVerifiedBeforeRetransmission() {
        assertEquals(PlanAction.VERIFY_EXISTING,action(assess().presence))
    }
    @Test fun absenceIsDistinctFromAmbiguousSourceWithLocalCandidate() {
        assertEquals(PlanAction.DOWNLOAD,action(assess(emptyList()).presence))
    }
    @Test fun nameAloneWrongSizeIsConflict() {
        val r=assess(listOf(local.copy(bytes=20))); assertEquals(LocalPresence.CHANGED,r.presence)
        assertEquals("LOCAL_METADATA_CONFLICT",r.matches.single().evidence)
        assertEquals(PlanAction.REVALIDATE_IDENTITY,action(r.presence))
    }
    @Test fun sizeAloneDifferentNameIsNotAssociation() {
        assertEquals(LocalPresence.ABSENT,assess(listOf(local.copy(displayName="different.mp4"))).presence)
    }
    @Test fun unrelatedPhoneFolderNeverMatches() {
        assertEquals(LocalPresence.ABSENT,assess(listOf(local.copy(directory="Movies/Other/"))).presence)
    }
    @Test fun lookalikeFolderDoesNotMatch() {
        assertFalse(LocalCandidatePolicy.inLandingZone("Movies/Osmosis-other/"))
        assertFalse(LocalCandidatePolicy.inLandingZone("Movies/Osmosis/../other/"))
    }
    @Test fun allocatedPathAndSizeCanRecoverRenamedDestination() {
        val r=assess(listOf(local.copy(directory="Movies/Osmosis/2026-09-17/",displayName="reserved.mp4")))
        assertEquals(LocalPresence.PRESENT_UNVERIFIED,r.presence)
        assertEquals("RESERVED_PATH_SIZE_PUBLISHED",r.matches.single().evidence)
    }
    @Test fun multipleMatchingLocalsRemainAmbiguous() {
        val r=assess(listOf(local,local.copy(locator="content://media/external_primary/file/8")))
        assertEquals(LocalPresence.AMBIGUOUS,r.presence)
        assertEquals(2,r.matches.size); assertEquals(PlanAction.REVALIDATE_IDENTITY,action(r.presence))
    }
    @Test fun duplicateProviderRowsAreNotTwoCandidates() {
        assertEquals(1,assess(listOf(local,local)).matches.size)
    }
    @Test fun pendingIsNeverCompleteCandidate() {
        assertEquals(LocalPresence.CHANGED,assess(listOf(local.copy(pending=true))).presence)
    }
    @Test fun unknownRemoteSizeCannotEstablishCompleteCandidate() {
        assertEquals(LocalPresence.CHANGED,LocalCandidatePolicy.assess("original.mp4",null,"x",LocalInventory(listOf(local),true)).presence)
    }
    @Test fun deletedCandidateRetainsMissingEvidence() {
        val r=assess(emptyList(),assess().matches)
        assertEquals(LocalPresence.MISSING,r.presence); assertEquals(local.locator,r.matches.single().media.locator)
        assertEquals(PlanAction.REVALIDATE_IDENTITY,action(r.presence,TransferState.LOCAL_PRESENT_UNVERIFIED))
    }
    @Test fun sameSizeReplacementInvalidatesPriorObservation() {
        val r=assess(listOf(local.copy(metadataVersion="replacement")),assess().matches)
        assertEquals(LocalPresence.CHANGED,r.presence)
    }
    @Test fun changedCandidateDoesNotSilentlyBecomeTrustedOnNextScan() {
        val replaced=local.copy(metadataVersion="replacement")
        val r=assess(listOf(replaced),assess().matches)
        assertEquals(LocalPresence.CHANGED,assess(listOf(replaced),r.matches).presence)
    }
    @Test fun sizeChangeAndRenameOfExistingUriRetainConflict() {
        assertEquals(LocalPresence.CHANGED,assess(listOf(local.copy(displayName="moved.mp4",bytes=42)),assess().matches).presence)
    }
    @Test fun inaccessibleProviderIsNotAbsence() {
        assertEquals(LocalPresence.UNAVAILABLE,assess(emptyList(),complete=false).presence)
        assertEquals(PlanAction.REVALIDATE_IDENTITY,action(LocalPresence.UNAVAILABLE))
    }
    @Test fun incompleteScanCannotAdoptEvenPlausibleCandidate() {
        assertEquals(LocalPresence.UNAVAILABLE,assess(complete=false).presence)
    }
    @Test fun repeatedUnchangedScanKeepsReferenceAndUnverifiedSemantics() {
        val first=assess(); assertEquals(first,assess(prior=first.matches))
        assertEquals(PlanAction.VERIFY_EXISTING,action(first.presence,TransferState.LOCAL_PRESENT_UNVERIFIED))
    }
    @Test fun noCandidateDispositionSuppressesWorkWithoutProof() {
        for(p in LocalPresence.entries) assertNotNull(action(p,TransferState.LOCAL_PRESENT_UNVERIFIED))
    }
    @Test fun validatedProofIsStillRequiredForVerifiedSkip() {
        assertNull(SyncPlanner.action(AssetClass.KNOWN_REQUIRED,TransferState.LOCAL_VERIFIED,false,true,LocalPresence.PRESENT_UNVERIFIED))
        assertNotNull(SyncPlanner.action(AssetClass.KNOWN_REQUIRED,TransferState.LOCAL_VERIFIED,false,false,LocalPresence.PRESENT_UNVERIFIED))
    }
    @Test fun MissingOrChangedEvidenceOverridesOldVerification() {
        for (p in listOf(LocalPresence.MISSING,LocalPresence.CHANGED,LocalPresence.UNAVAILABLE,LocalPresence.AMBIGUOUS))
            assertEquals(PlanAction.REVALIDATE_IDENTITY,SyncPlanner.action(AssetClass.KNOWN_REQUIRED,TransferState.LOCAL_VERIFIED,false,true,p))
    }
}
