package dev.konraditurbe.osmosis.integrity
import org.junit.Assert.*
import org.junit.Test
class RecoveryPolicyTest {
    @Test fun orphanUnavailableAndMismatchNeverAuthorizeResume() {
        assertEquals(RecoveryAction.REVIEW_ORPHAN_INTENT,RecoveryPolicy.decide(false,0,100,null,false,false))
        assertEquals(RecoveryAction.PRESERVE_UNAVAILABLE,RecoveryPolicy.decide(true,50,100,null,false,false))
        for(n in listOf(-1L,49L,51L,101L))assertEquals(RecoveryAction.REVIEW_LENGTH_MISMATCH,RecoveryPolicy.decide(true,50,100,n,false,false))
    }
    @Test fun partialRequiresRevalidationEvenAtExpectedLength() {
        for(n in listOf(0L,50L,100L))assertEquals(RecoveryAction.REVALIDATE_PARTIAL,RecoveryPolicy.decide(true,n,100,n,false,false))
    }
    @Test fun publishedFileDoesNotImplyVerified() {
        assertEquals(RecoveryAction.REVALIDATE_PUBLICATION,RecoveryPolicy.decide(true,100,100,100,true,true))
        assertEquals(RecoveryAction.PRESERVE_PUBLISHED,RecoveryPolicy.decide(true,100,100,100,true,false))
    }
}
