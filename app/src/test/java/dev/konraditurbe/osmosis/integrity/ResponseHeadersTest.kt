package dev.konraditurbe.osmosis.integrity

import org.junit.Assert.*
import org.junit.Test

class ResponseHeadersTest {
    @Test fun normalHeadersAreCaseInsensitiveAndAllowOws(){
        val parsed=ResponseHeaders.parse(206,mapOf("content-length" to listOf("\t3 "),"CONTENT-RANGE" to listOf("bytes 3-5/6"),"ETag" to listOf("opaque")))
        assertEquals(ValidatedRange(3,5,6),RangeContract.validate(6,3,parsed.metadata));assertTrue(parsed.etagPresent)
    }
    @Test fun duplicateAndCaseVariantDuplicatesAreRejected(){
        for(headers in listOf(mapOf("Content-Length" to listOf("3","3")),mapOf("Content-Length" to listOf("3"),"content-length" to listOf("4")),mapOf("Content-Range" to listOf("bytes 3-5/6","bytes 0-2/6"))))
            assertTrue(runCatching{ResponseHeaders.parse(206,headers)}.isFailure)
    }
    @Test fun contradictoryTransferCodingIsRejectedBeforeBody(){
        for(coding in listOf("chunked","identity",""))assertTrue(runCatching {
            ResponseHeaders.parse(200,mapOf("Content-Length" to listOf("6"),"Transfer-Encoding" to listOf(coding)))
        }.isFailure)
    }
    @Test fun invalidLengthAndHeaderControlCharactersAreRejected(){
        for(length in listOf("-1","+3","3,3","9223372036854775808","3\n","","3.0"))
            assertTrue(runCatching{ResponseHeaders.parse(200,mapOf("Content-Length" to listOf(length)))}.isFailure)
        assertTrue(runCatching{ResponseHeaders.parse(200,mapOf("ETag" to listOf("a\r\nb")))}.isFailure)
    }
    @Test fun missingOrWeakVersionNeverCreatesSourceProof(){
        val parsed=ResponseHeaders.parse(200,mapOf("ETag" to listOf("W/\"fixture\"")))
        assertTrue(parsed.etagPresent);assertNull(parsed.metadata.contentLength)
        assertNull(RangeContract.validate(6,0,parsed.metadata))
        assertEquals(OverallVerification.UNVERIFIED,VerificationDimensions(EvidenceResult.CONFIRMED,EvidenceResult.UNCONFIRMED).overall)
    }
    @Test fun framingRemains64Bit(){
        val value=5L*1024*1024*1024
        val parsed=ResponseHeaders.parse(200,mapOf("Content-Length" to listOf(value.toString())))
        assertEquals(value,parsed.metadata.contentLength)
        assertEquals(value,RangeContract.validate(value,0,parsed.metadata)!!.responseLength)
    }
}
