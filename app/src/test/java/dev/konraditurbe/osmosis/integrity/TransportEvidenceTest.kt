package dev.konraditurbe.osmosis.integrity
import org.junit.Assert.*
import org.junit.Test

class TransportEvidenceTest {
    @Test fun untrustedHeaderContentsNeverEnterEvidence(){
        val result=TransportEvidence.summary(ResponseMetadata(206,42,"private-path-token\nGPS","private-encoding"),100,true)
        assertEquals("HTTP status=206 length=42 offset=100 range_present=true identity_encoding=false etag_present=true",result)
        assertFalse(result.contains("private"));assertFalse(result.contains("GPS"))
    }
    @Test fun missingVersionIsExplicitWithoutClaimingEquivalence(){
        assertEquals("HTTP status=200 length=null offset=0 range_present=false identity_encoding=true etag_present=false",
            TransportEvidence.summary(ResponseMetadata(200,null,null),0,false))
    }
}
