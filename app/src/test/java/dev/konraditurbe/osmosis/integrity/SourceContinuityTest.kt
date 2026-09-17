package dev.konraditurbe.osmosis.integrity

import org.junit.Assert.assertEquals
import org.junit.Test

class SourceContinuityTest {
    private val stable=SourceObservation("pocket","asset-1","/v2?file=one",100,"v1",VersionTrust.IMMUTABLE_VERSION)
    @Test fun sameTrustedObjectAcrossRequestsIsConfirmed() {
        assertEquals(Continuity.CONFIRMED,SourceContinuity.evaluate(stable,stable.copy()))
    }
    @Test fun changedSourceFactsAreContradictedBeforeAppend() {
        for (changed in listOf(stable.copy(source="other"),stable.copy(asset="other"),
            stable.copy(remotePath="/v2?file=other"),stable.copy(bytes=101),stable.copy(validator="v2")))
            assertEquals(Continuity.CONTRADICTED,SourceContinuity.evaluate(stable,changed))
    }
    @Test fun missingOrUntrustedValidatorIsUnconfirmed() {
        assertEquals(Continuity.UNCONFIRMED,SourceContinuity.evaluate(stable.copy(validator=null),stable.copy(validator=null)))
        assertEquals(Continuity.UNCONFIRMED,SourceContinuity.evaluate(stable.copy(validatorTrust=VersionTrust.UNKNOWN),stable.copy(validatorTrust=VersionTrust.UNKNOWN)))
    }
    @Test fun filenameReuseCannotConfirmContinuity() {
        val first=stable.copy(validator=null,validatorTrust=VersionTrust.UNKNOWN)
        assertEquals(Continuity.UNCONFIRMED,SourceContinuity.evaluate(first,first.copy()))
    }
    @Test fun reconnectWithoutValidatorRemainsUnconfirmedEvenWithCorrectRangeSize() {
        val response=ResponseMetadata(206,60,"bytes 40-99/100")
        assertEquals(ValidatedRange(40,99,100),RangeContract.validate(100,40,response))
        val noValidator=stable.copy(validator=null,validatorTrust=VersionTrust.UNKNOWN)
        assertEquals(Continuity.UNCONFIRMED,SourceContinuity.evaluate(noValidator,noValidator.copy()))
    }
}
