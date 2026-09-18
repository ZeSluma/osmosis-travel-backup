package dev.konraditurbe.osmosis.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** Deterministic provider-fault matrix; it deliberately makes no claim about a real SAF provider. */
class ExternalProviderFaultScenarioTest {
    private class RenameFailingPending : PendingReplica {
        private val bytes = ByteArrayOutputStream()
        override val locator = "content://synthetic/owned-part"
        override fun output(): OutputStream = bytes
        override fun input(): InputStream = ByteArrayInputStream(bytes.toByteArray())
        override fun sync() = Unit
        override fun bytes(): Long = bytes.size().toLong()
        override fun finalizeReplica(): Unit = throw IOException("synthetic rename failure")
    }

    @Test fun providerDenialFinalizationFailureAndReappearanceRemainFailClosed() {
        assertEquals(ExternalStorageAvailability.PERMISSION_REQUIRED, ExternalStoragePolicy.assess(false, 100, 10))
        assertEquals(ExternalStorageAvailability.UNAVAILABLE, ExternalStoragePolicy.assess(true, null, 10))
        assertEquals(ExternalStorageAvailability.INSUFFICIENT_SPACE, ExternalStoragePolicy.assess(true, 9, 10))

        val content = byteArrayOf(1, 2, 3)
        val proof = ReplicaProof(3, ReplicaVerification.hex(java.security.MessageDigest.getInstance("SHA-256").digest(content)))
        val result = ReplicaVerification.copy(proof, { ByteArrayInputStream(content) }, RenameFailingPending())
        assertTrue(result is ReplicaVerification.Result.Incomplete)

        assertFalse(ExternalReplicaRecoveryPolicy.mayAllocate(listOf("UNAVAILABLE")))
        assertFalse(ExternalReplicaRecoveryPolicy.mayAllocate(listOf("PARTIAL")))
        // Reappearance alone is not enough: only an explicit owned-object observation of MISSING reopens work.
        assertTrue(ExternalReplicaRecoveryPolicy.mayAllocate(listOf(StagedReplicaObservation.MISSING.name)))
    }
}
