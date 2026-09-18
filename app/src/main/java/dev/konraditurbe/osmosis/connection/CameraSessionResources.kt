package dev.konraditurbe.osmosis.connection

import android.net.Network
import dev.konraditurbe.osmosis.core.MediaSession
import dev.konraditurbe.osmosis.net.ApJoiner
import java.util.concurrent.atomic.AtomicInteger

/**
 * Mutable camera-only transport resources owned by the application/service connection layer.
 * They intentionally contain neither UI objects nor cloud/default-network state. Every caller
 * must additionally use [DurableSessionRuntime]'s epoch fence before accepting their results.
 */
class CameraSessionResources {
    var apJoiner: ApJoiner? = null
    @Volatile var transferNetwork: Network? = null
    var wifiUp = false
    var datalinkStarted = false
    var wifiRejoins = 0
    var resumeDownloadOnRejoin = false
    val datalinkGeneration = AtomicInteger(0)
    @Volatile var pendingSession: MediaSession? = null
    var datalink: MediaSession? = null

    fun releaseTransport() {
        datalinkGeneration.incrementAndGet()
        runCatching { pendingSession?.close() }; pendingSession = null
        runCatching { datalink?.close() }; datalink = null
        runCatching { apJoiner?.release() }; apJoiner = null
        transferNetwork = null
        wifiUp = false; datalinkStarted = false; wifiRejoins = 0; resumeDownloadOnRejoin = false
    }
}
