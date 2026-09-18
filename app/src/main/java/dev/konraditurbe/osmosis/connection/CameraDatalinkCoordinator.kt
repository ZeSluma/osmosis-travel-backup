package dev.konraditurbe.osmosis.connection

import dev.konraditurbe.osmosis.ble.CameraModel
import dev.konraditurbe.osmosis.core.CameraFile
import dev.konraditurbe.osmosis.core.CameraStatus
import dev.konraditurbe.osmosis.core.MediaSession
import dev.konraditurbe.osmosis.ledger.LedgerEnumerator
import java.time.Instant

/** Service-owned datalink socket lifecycle; callbacks expose observations, never mutable ownership. */
class CameraDatalinkCoordinator(
    private val resources: CameraSessionResources,
    private val sessions: CameraSessionEffectCoordinator,
) {
    data class Observation(
        val session: MediaSession,
        val files: List<CameraFile>,
        val enumerationFailed: Boolean,
        val enumerationStarted: Instant,
    )

    fun start(
        epoch: Long,
        model: CameraModel,
        openSession: (CameraModel) -> MediaSession,
        onLog: (String) -> Unit,
        onStatus: (CameraStatus) -> Unit,
        onProgress: (Int) -> Unit,
        onReady: (Observation) -> Unit,
    ) {
        val generation = resources.datalinkGeneration.incrementAndGet()
        Thread {
            var failed = false
            var started = Instant.now()
            fun open(candidate: CameraModel): Pair<MediaSession, List<CameraFile>> {
                onLog("=== media list [${candidate.name}] via udp/${candidate.datalinkPort} (poke=${candidate.tcpPoke}) ===")
                val session = openSession(candidate)
                session.onStatus = onStatus
                session.onFetchProgress = { progress -> onProgress(60 + progress * 38 / 100) }
                resources.pendingSession = session
                started = Instant.now()
                val enumeration = LedgerEnumerator.enumerate(session)
                failed = enumeration.failed
                return session to enumeration.files
            }
            fun superseded(session: MediaSession): Boolean {
                if (generation == resources.datalinkGeneration.get()) return false
                onLog("datalink: superseded by a newer connection — dropping its session")
                runCatching { session.close() }
                return true
            }
            resources.datalink?.close()
            var (datalink, files) = open(model)
            if (superseded(datalink)) return@Thread
            if (!datalink.handshakeOk) {
                val alternate = model.alternate()
                onLog("datalink: nothing answered on udp/${model.datalinkPort} — trying udp/${alternate.datalinkPort}")
                runCatching { datalink.close() }
                val retried = open(alternate)
                datalink = retried.first
                files = retried.second
                if (datalink.handshakeOk) onLog("datalink: ${model.name} answered alternate udp/${alternate.datalinkPort}")
            }
            if (superseded(datalink)) return@Thread
            resources.pendingSession = null
            resources.datalink = datalink
            datalink.startKeepAlive()
            sessions.revalidate(epoch, !datalink.moreAvailable, failed || !datalink.handshakeOk)
            onReady(Observation(datalink, files, failed, started))
        }.start()
    }
}
