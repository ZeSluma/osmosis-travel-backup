package dev.konraditurbe.osmosis.integrity

import org.junit.Assert.*
import org.junit.Test

class IntegrityContractTest {
    private val local = LocalBinding("synthetic", "revision1", 100, true, true)
    private val whole = TransferReceipt(100,0,100,ResponseMetadata(200,100,null),false,false,true,true,true,true)
    @Test fun orthogonalTruthTableFailsClosed() {
        for(t in EvidenceResult.entries) for(s in EvidenceResult.entries) {
            val d=VerificationDimensions(t,s)
            assertEquals(t==EvidenceResult.CONFIRMED && s==EvidenceResult.CONFIRMED,d.overall==OverallVerification.VERIFIED)
            if(s!=EvidenceResult.CONFIRMED) {
                assertFalse(CompletionEligibility.cameraSyncComplete(true,true,listOf(d)))
                assertFalse(CompletionEligibility.redundancyComplete(true,true,listOf(d)))
                assertFalse(CompletionEligibility.safeToClearPrerequisites(true,true,listOf(d),true,listOf(d,d),true))
            }
        }
    }
    @Test fun fullTransferCanConfirmWithoutSourceProof() {
        val t=IntegrityContract.transfer(whole,local)
        assertEquals(EvidenceResult.CONFIRMED,t)
        assertEquals(OverallVerification.UNVERIFIED,VerificationDimensions(t,EvidenceResult.UNCONFIRMED).overall)
    }
    @Test fun missingCoverageOrIndependentReplicaNeverCompletes() {
        val v=listOf(VerificationDimensions(EvidenceResult.CONFIRMED,EvidenceResult.CONFIRMED))
        assertFalse(CompletionEligibility.cameraSyncComplete(false,true,v))
        assertFalse(CompletionEligibility.cameraSyncComplete(true,false,v))
        assertFalse(CompletionEligibility.redundancyComplete(true,false,v))
        assertFalse(CompletionEligibility.redundancyComplete(true,true,emptyList()))
    }
    @Test fun validFullAndResumeRanges() {
        assertNotNull(RangeContract.validate(100,0,ResponseMetadata(200,100,null)))
        assertNotNull(RangeContract.validate(100,40,ResponseMetadata(206,60,"bytes 40-99/100")))
        assertNotNull(RangeContract.validate(Long.MAX_VALUE,Long.MAX_VALUE-1,ResponseMetadata(206,1,"bytes 9223372036854775806-9223372036854775806/9223372036854775807")))
    }
    @Test fun badRangesNeverAuthorizeWrites() {
        for(r in listOf(ResponseMetadata(200,60,null),ResponseMetadata(416,60,"bytes */100"),
            ResponseMetadata(206,null,"bytes 40-99/100"),ResponseMetadata(206,60,"bytes 39-99/100"),
            ResponseMetadata(206,60,"bytes 40-98/100"),ResponseMetadata(206,60,"bytes 40-99/101"),
            ResponseMetadata(206,60,"bytes 40-99/*"),ResponseMetadata(206,60,"bytes 40-99/9223372036854775808"),
            ResponseMetadata(206,60,"bytes 40-99/100","gzip"),ResponseMetadata(500,60,null)))
            assertNull(RangeContract.validate(100,40,r))
        assertNull(RangeContract.validate(0,0,ResponseMetadata(200,0,null)))
        assertNull(RangeContract.validate(100,100,ResponseMetadata(416,0,null)))
        assertNull(RangeContract.validate(100,-1,ResponseMetadata(200,100,null)))
    }
    @Test fun truncatedExtraFlushCloseReadbackAndUnreadableFail() {
        for(r in listOf(whole.copy(received=99),whole.copy(received=101),whole.copy(eof=false),
            whole.copy(flushed=false),whole.copy(closed=false),whole.copy(readBackConfirmed=false)))
            assertEquals(EvidenceResult.FAILED,IntegrityContract.transfer(r,local))
        for(l in listOf(local.copy(bytes=99),local.copy(readable=false),local.copy(revision="")))
            assertEquals(EvidenceResult.FAILED,IntegrityContract.transfer(whole,l))
    }
    @Test fun resumeNeedsBothPrefixAndContinuityProof() {
        val r=whole.copy(offset=40,received=60,response=ResponseMetadata(206,60,"bytes 40-99/100"))
        assertEquals(EvidenceResult.UNCONFIRMED,IntegrityContract.transfer(r,local))
        assertEquals(EvidenceResult.UNCONFIRMED,IntegrityContract.transfer(r.copy(prefixIntegrityConfirmed=true),local))
        assertEquals(EvidenceResult.CONFIRMED,IntegrityContract.transfer(r.copy(prefixIntegrityConfirmed=true,prefixSourceContinuityConfirmed=true),local))
    }
}
