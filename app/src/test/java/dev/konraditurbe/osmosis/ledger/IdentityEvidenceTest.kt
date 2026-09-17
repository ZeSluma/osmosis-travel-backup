package dev.konraditurbe.osmosis.ledger

import org.junit.Assert.*
import org.junit.Test

class IdentityEvidenceTest {
    private val observation=IdentityObservationRow("o","s","snap","DCIM/a.mp4","1",null,null,"VIDEO",null,null,"UNRESOLVED",null)
    private val asset=AssetRow("a","s","r","f","1","DCIM/a.mp4",100,null,"VIDEO",null,"v1","KNOWN_REQUIRED","test",false,1)
    @Test fun missingOrInvalidLengthIsNotActionable(){ for(s in listOf(null,0L,-1L))assertFalse(IdentityEvidence.materializable(s));assertTrue(IdentityEvidence.materializable(100)) }
    @Test fun nameAloneCannotResolve(){assertFalse(IdentityEvidence.provesSameVersion(observation,asset))}
    @Test fun exactVersionWithCompatibleEvidenceCanLink(){assertTrue(IdentityEvidence.provesSameVersion(observation.copy(strongVersion="v1"),asset))}
    @Test fun differentVersionOrSourceCannotLink(){val o=observation.copy(strongVersion="v1");assertFalse(IdentityEvidence.provesSameVersion(o,asset.copy(strongVersion="v2")));assertFalse(IdentityEvidence.provesSameVersion(o,asset.copy(sourceId="other")))}
    @Test fun locatorStorageTypeAndKnownSizeMustAgree(){val o=observation.copy(strongVersion="v1");for(a in listOf(asset.copy(remotePath="other/a.mp4"),asset.copy(storage="2"),asset.copy(mediaType="RAW"),asset.copy(size=null)))assertFalse(IdentityEvidence.provesSameVersion(o,a));assertFalse(IdentityEvidence.provesSameVersion(o.copy(size=101),asset))}
    @Test fun contradictoryKnownMetadataCannotBeIgnored(){val o=observation.copy(strongVersion="v1");assertFalse(IdentityEvidence.provesSameVersion(o.copy(remoteTime="changed"),asset));assertFalse(IdentityEvidence.provesSameVersion(o.copy(handleEvidence="changed"),asset))}
}
