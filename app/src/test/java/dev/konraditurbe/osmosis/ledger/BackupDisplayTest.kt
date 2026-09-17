package dev.konraditurbe.osmosis.ledger
import dev.konraditurbe.osmosis.core.CameraFile
import org.junit.Assert.*
import org.junit.Test

class BackupDisplayTest {
    @Test fun completedAndPartialHardwareStatesStayDistinct(){
        assertEquals(BackupDisplayState.TRANSFERRED_UNVERIFIED,
            BackupDisplayPolicy.resolve("TRANSFERRED_UNVERIFIED","PRESENT_UNVERIFIED",true,103945077,103945077,true).state)
        assertEquals(BackupDisplay(BackupDisplayState.PARTIAL_REVIEW,28),
            BackupDisplayPolicy.resolve("PARTIAL","CHANGED",true,330769591,1164588515))
    }
    @Test fun publicationGapAndFullLengthPartialNeverLookComplete(){
        assertEquals(BackupDisplayState.REVIEW_REQUIRED,BackupDisplayPolicy.resolve("TRANSFERRED_UNVERIFIED","NOT_SCANNED",true,100,100,false).state)
        assertEquals(BackupDisplayState.REVIEW_REQUIRED,BackupDisplayPolicy.resolve("PARTIAL","NOT_SCANNED",true,100,100).state)
    }
    @Test fun missingOrAmbiguousBindingsOverrideOldProgress(){
        for(p in listOf("MISSING","UNAVAILABLE","AMBIGUOUS"))
            assertEquals(BackupDisplayState.REVIEW_REQUIRED,BackupDisplayPolicy.resolve("PARTIAL",p,true,40,100).state)
    }
    @Test fun existingCopiesNeverAcquireTransferOrVerificationClaims(){
        assertEquals(BackupDisplayState.EXISTING_UNVERIFIED,BackupDisplayPolicy.resolve("LOCAL_PRESENT_UNVERIFIED","PRESENT_UNVERIFIED",true,0,100).state)
        assertEquals(BackupDisplayState.REVIEW_REQUIRED,BackupDisplayPolicy.resolve("LOCAL_VERIFIED","NOT_SCANNED",true,100,100).state)
    }
    @Test fun unknownOrInvalidRecordsCannotClaimNewOrCompleted(){
        assertEquals(BackupDisplayState.REVIEW_REQUIRED,BackupDisplayPolicy.resolve("PARTIAL","NOT_SCANNED",true,-1,100).state)
        assertEquals(BackupDisplayState.REVIEW_REQUIRED,BackupDisplayPolicy.resolve("DISCOVERED","NOT_SCANNED",false,0,null).state)
        assertEquals(BackupDisplayState.NEW,BackupDisplayPolicy.resolve("DISCOVERED","ABSENT",false,0,100).state)
    }
    @Test fun displayKeysDoNotMatchSolelyByFilename(){
        val file=CameraFile("same.MP4","",storage=0,sizeBytes=100)
        assertNotEquals(LedgerCoordinator.displayKey(file),LedgerCoordinator.displayKey(file.copy(storage=1)))
        assertNotEquals(LedgerCoordinator.displayKey(file),LedgerCoordinator.displayKey(file.copy(sizeBytes=200)))
    }
}
