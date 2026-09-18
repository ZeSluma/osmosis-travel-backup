package dev.konraditurbe.osmosis.connection

import dev.konraditurbe.osmosis.ble.CameraModel
import dev.konraditurbe.osmosis.core.CameraFile
import dev.konraditurbe.osmosis.core.CameraStatus
import dev.konraditurbe.osmosis.core.MediaSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CameraDatalinkCoordinatorTest {
    private class Store : SessionStore {
        var value = SessionLease(); private var transfer: TransferLease? = null; private var generation = 0L
        override fun read() = value
        override fun write(value: SessionLease) { this.value = value }
        override fun readTransfer() = transfer
        override fun writeTransfer(value: TransferLease?) { transfer = value }
        override fun nextTransferGeneration() = ++generation
    }
    private class FakeSession(private val pages: List<List<CameraFile>>) : MediaSession {
        override var onStatus: ((CameraStatus) -> Unit)? = null
        override var onFetchProgress: ((Int) -> Unit)? = null
        override val handshakeOk = true
        private var index = 0
        override val moreAvailable get() = index < pages.lastIndex
        override fun fetchFileList(ip: String): List<CameraFile> = pages.first()
        override fun fetchNextPage(): List<CameraFile> = pages[++index]
        override fun startKeepAlive() = Unit
        override fun close() = Unit
    }

    @Test fun emptyPostConnectInventoryIsRetriedThenRemainsUntrustedAndNotReady() {
        val runtime = CameraSessionEffectCoordinator(DurableSessionRuntime(Store()))
        val epoch = runtime.begin().epoch
        runtime.transportReady(epoch)
        var opens = 0
        val done = CountDownLatch(1)
        var observation: CameraDatalinkCoordinator.Observation? = null
        CameraDatalinkCoordinator(CameraSessionResources(), runtime).start(epoch, CameraModel.DEFAULT,
            openSession = { FakeSession(listOf(emptyList<CameraFile>())).also { opens++ } }, onLog = {}, onStatus = {}, onProgress = {},
            onReady = { observation = it; done.countDown() })
        assertTrue(done.await(2, TimeUnit.SECONDS))
        assertEquals(2, opens)
        assertTrue(checkNotNull(observation).files.isEmpty())
        assertFalse(checkNotNull(observation).sourceTrusted)
        assertFalse(checkNotNull(observation).enumerationFailed)
        assertEquals(ConnectionState.REVALIDATING, runtime.snapshot().recovery.state)
        assertFalse(CameraSessionCoordinator.mayUseCameraTraffic(runtime.snapshot()))
    }

    @Test fun laterNonemptyCompleteRevalidationBecomesTrustedAfterPriorEmptyAttempt() {
        val runtime = CameraSessionEffectCoordinator(DurableSessionRuntime(Store()))
        val epoch = runtime.begin().epoch
        runtime.transportReady(epoch)
        var opens = 0
        val done = CountDownLatch(1)
        var observation: CameraDatalinkCoordinator.Observation? = null
        val file = CameraFile(path = "/DCIM/test.mp4", thumbPath = "/MISC/test.thm", sizeBytes = 100)
        CameraDatalinkCoordinator(CameraSessionResources(), runtime).start(epoch, CameraModel.DEFAULT,
            openSession = {
                opens++
                FakeSession(listOf(if (opens == 1) emptyList() else listOf(file)))
            }, onLog = {}, onStatus = {}, onProgress = {}, onReady = { observation = it; done.countDown() })
        assertTrue(done.await(2, TimeUnit.SECONDS))
        assertEquals(2, opens)
        assertTrue(checkNotNull(observation).sourceTrusted)
        assertTrue(checkNotNull(observation).enumerationComplete)
        assertEquals(ConnectionState.READY, runtime.snapshot().recovery.state)
    }

    @Test fun nonemptyCallbackFromSupersededEpochCannotPublishTrustedInventory() {
        val runtime = CameraSessionEffectCoordinator(DurableSessionRuntime(Store()))
        val stale = runtime.begin().epoch
        val current = runtime.begin().epoch
        runtime.transportReady(current)
        val done = CountDownLatch(1)
        var observation: CameraDatalinkCoordinator.Observation? = null
        val file = CameraFile(path = "/DCIM/test.mp4", thumbPath = "/MISC/test.thm", sizeBytes = 100)
        CameraDatalinkCoordinator(CameraSessionResources(), runtime).start(stale, CameraModel.DEFAULT,
            openSession = { FakeSession(listOf(listOf(file))) }, onLog = {}, onStatus = {}, onProgress = {},
            onReady = { observation = it; done.countDown() })
        // A superseded observation is not merely marked untrusted: it is never delivered to a
        // caller that could repaint a replacement UI or attach it to a replacement ledger session.
        assertFalse(done.await(250, TimeUnit.MILLISECONDS))
        assertEquals(null, observation)
        assertEquals(current, runtime.snapshot().epoch)
        assertEquals(ConnectionState.REVALIDATING, runtime.snapshot().recovery.state)
    }
}
