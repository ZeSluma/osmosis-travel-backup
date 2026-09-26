package dev.konraditurbe.osmosis.integrity

import java.io.ByteArrayInputStream
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

class CameraTransferSourceTest {
    @Test fun strictTransferRetriesTransientRefusalBeforeAnyByteSourceIsReturned() {
        var attempts = 0
        var retries = 0
        val source = CameraTransferSource(
            network = null,
            opener = { _, _ -> response(if (++attempts < 3) 404 else 200) },
            sleeper = {},
        )

        val response = source.openForStrictTransfer("/v2?media/clip.mp4", cancelled = { false }) { retries++ }

        assertEquals(3, attempts)
        assertEquals(2, retries)
        assertEquals(200, response.metadata.status)
        response.close()
    }

    @Test fun strictTransferStopsAfterBoundedTransientRefusalsAndNeverClaimsSuccess() {
        var attempts = 0
        var retries = 0
        val source = CameraTransferSource(
            network = null,
            opener = { _, _ -> attempts++; response(404) },
            sleeper = {},
        )

        try {
            source.openForStrictTransfer("/v2?media/clip.mp4", cancelled = { false }) { retries++ }
            throw AssertionError("Expected bounded refusal to fail closed")
        } catch (error: IOException) {
            assertEquals("UNUSABLE_HTTP_STATUS", error.message)
        }
        assertEquals(4, attempts) // initial request plus three bounded retries
        assertEquals(3, retries)
    }

    @Test fun strictTransferDoesNotRetryAnUnrelatedClientError() {
        var attempts = 0
        val source = CameraTransferSource(
            network = null,
            opener = { _, _ -> attempts++; response(403) },
            sleeper = {},
        )

        try {
            source.openForStrictTransfer("/v2?media/clip.mp4", cancelled = { false })
            throw AssertionError("Expected client refusal to fail closed")
        } catch (error: IOException) {
            assertEquals("UNUSABLE_HTTP_STATUS", error.message)
        }
        assertEquals(1, attempts)
    }

    private fun response(status: Int): TransferResponse = object : TransferResponse {
        override val metadata = ResponseMetadata(status, if (status == 200) 1L else null, null, "identity")
        override fun input() = ByteArrayInputStream(byteArrayOf(1))
        override fun close() = Unit
    }
}
