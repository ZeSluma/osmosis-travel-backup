package dev.konraditurbe.osmosis.connection

import android.net.Network
import dev.konraditurbe.osmosis.core.MediaSession
import dev.konraditurbe.osmosis.core.CameraFile
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
    @Volatile var sourceAssociation: String? = null
    @Volatile var trustedFilesByPath: Map<String, CameraFile> = emptyMap()
    @Volatile var automaticStrictTransferSupported = false
    var apJoiner: ApJoiner? = null
    @Volatile var transferNetwork: Network? = null
    var wifiUp = false
    var datalinkStarted = false
    var wifiRejoins = 0
    var resumeDownloadOnRejoin = false
    val datalinkGeneration = AtomicInteger(0)
    /** Scan callbacks can arrive after stopScan; fence them before they reach selection logic. */
    private val scannerCallbackGeneration = AtomicInteger(0)
    /** Network callbacks can arrive after request cancellation or a replacement AP request. */
    private val apJoinerCallbackGeneration = AtomicInteger(0)
    /** Every GATT listener is fenced independently of the media-session generation. */
    private val gattCallbackGeneration = AtomicInteger(0)
    @Volatile var pendingSession: MediaSession? = null
    var datalink: MediaSession? = null

    fun nextGattCallbackGeneration(): Int = gattCallbackGeneration.incrementAndGet()
    fun acceptsGattCallback(generation: Int): Boolean = generation == gattCallbackGeneration.get()
    fun nextScannerCallbackGeneration(): Int = scannerCallbackGeneration.incrementAndGet()
    fun acceptsScannerCallback(generation: Int): Boolean = generation == scannerCallbackGeneration.get()
    fun nextApJoinerCallbackGeneration(): Int = apJoinerCallbackGeneration.incrementAndGet()
    fun acceptsApJoinerCallback(generation: Int): Boolean = generation == apJoinerCallbackGeneration.get()

    fun releaseScanner() {
        scannerCallbackGeneration.incrementAndGet()
        val previous = scanner
        scanner = null
        runCatching { previous?.stop() }
    }

    fun releaseApJoiner() {
        apJoinerCallbackGeneration.incrementAndGet()
        val previous = apJoiner
        apJoiner = null
        runCatching { previous?.release() }
    }

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
        releaseScanner()
        releaseGatt()
        runCatching { pendingSession?.close() }; pendingSession = null
        runCatching { datalink?.close() }; datalink = null
        releaseApJoiner()
        transferNetwork = null
        ledgerSession = null
        sourceAssociation = null
        trustedFilesByPath = emptyMap()
        automaticStrictTransferSupported = false
        wifiUp = false; datalinkStarted = false; wifiRejoins = 0; resumeDownloadOnRejoin = false
    }
}
