package dev.konraditurbe.osmosis.connection

import android.net.Network
import dev.konraditurbe.osmosis.core.MediaSession
import dev.konraditurbe.osmosis.ble.GattClient
import dev.konraditurbe.osmosis.ble.OsmoScanner
import dev.konraditurbe.osmosis.net.ApJoiner
import java.util.concurrent.atomic.AtomicInteger

/**
 * Mutable camera-only transport resources owned by the application/service connection layer.
 * They intentionally contain neither UI objects nor cloud/default-network state. Every caller
 * must additionally use [DurableSessionRuntime]'s epoch fence before accepting their results.
 */
class CameraSessionResources {
    var scanner: OsmoScanner? = null
    var gattClient: GattClient? = null
    var connecting = false
    var ledgerSession: String? = null
    var apJoiner: ApJoiner? = null
    @Volatile var transferNetwork: Network? = null
    var wifiUp = false
    var datalinkStarted = false
    var wifiRejoins = 0
    var resumeDownloadOnRejoin = false
    val datalinkGeneration = AtomicInteger(0)
    /** Every GATT listener is fenced independently of the media-session generation. */
    private val gattCallbackGeneration = AtomicInteger(0)
    @Volatile var pendingSession: MediaSession? = null
    var datalink: MediaSession? = null

    fun nextGattCallbackGeneration(): Int = gattCallbackGeneration.incrementAndGet()
    fun acceptsGattCallback(generation: Int): Boolean = generation == gattCallbackGeneration.get()

    /**
     * Invalidate before closing: Android may deliver the old disconnect asynchronously after a
     * replacement connection has been installed.  That callback must not tear down the new session.
     */
    fun releaseGatt() {
        gattCallbackGeneration.incrementAndGet()
        val previous = gattClient
        gattClient = null
        runCatching { previous?.disconnect() }
        runCatching { previous?.close() }
    }

    fun releaseTransport() {
        datalinkGeneration.incrementAndGet()
        runCatching { scanner?.stop() }; scanner = null
        releaseGatt()
        runCatching { pendingSession?.close() }; pendingSession = null
        runCatching { datalink?.close() }; datalink = null
        runCatching { apJoiner?.release() }; apJoiner = null
        transferNetwork = null
        ledgerSession = null
        wifiUp = false; datalinkStarted = false; wifiRejoins = 0; resumeDownloadOnRejoin = false
    }
}
