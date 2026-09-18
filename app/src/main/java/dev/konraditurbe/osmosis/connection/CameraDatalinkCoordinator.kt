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
        val enumerationComplete: Boolean,
        val enumerationFailed: Boolean,
        /** A zero-result response is not authoritative after automatic connection/recovery. */
        val sourceTrusted: Boolean,
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
            var started = Instant.now()
            fun open(candidate: CameraModel): Pair<MediaSession, dev.konraditurbe.osmosis.ledger.EnumerationBatch> {
                onLog("=== media list [${candidate.name}] via udp/${candidate.datalinkPort} (poke=${candidate.tcpPoke}) ===")
                val session = openSession(candidate)
                session.onStatus = onStatus
                session.onFetchProgress = { progress -> onProgress(60 + progress * 38 / 100) }
                resources.pendingSession = session
                started = Instant.now()
                val enumeration = LedgerEnumerator.enumerate(session)
                return session to enumeration
            }
            fun superseded(session: MediaSession): Boolean {
                if (generation == resources.datalinkGeneration.get()) return false
                onLog("datalink: superseded by a newer connection — dropping its session")
                runCatching { session.close() }
                return true
            }
            resources.datalink?.close()
            var (datalink, enumeration) = open(model)
            if (superseded(datalink)) return@Thread
            if (!datalink.handshakeOk) {
                val alternate = model.alternate()
                onLog("datalink: nothing answered on udp/${model.datalinkPort} — trying udp/${alternate.datalinkPort}")
                runCatching { datalink.close() }
                val retried = open(alternate)
                datalink = retried.first
                enumeration = retried.second
                if (datalink.handshakeOk) onLog("datalink: ${model.name} answered alternate udp/${alternate.datalinkPort}")
            }
            if (superseded(datalink)) return@Thread
            // A transport-ready Pocket can transiently answer the first media query with no
            // entries.  It is unsafe to call that a complete empty source: retry once with a
            // fresh protocol session, then leave the source explicitly untrusted if it remains
            // empty or incomplete.  This is bounded and does not turn into a background loop.
            if (datalink.handshakeOk && (enumeration.files.isEmpty() || !enumeration.pagesEnded || enumeration.failed)) {
                onLog("datalink: inventory incomplete/empty after session-ready — one bounded revalidation retry")
                runCatching { datalink.close() }
                val retried = open(model)
                datalink = retried.first
                enumeration = retried.second
                if (superseded(datalink)) return@Thread
            }
            resources.pendingSession = null
            resources.datalink = datalink
            datalink.startKeepAlive()
            val candidateTrusted = datalink.handshakeOk && enumeration.pagesEnded && !enumeration.failed && enumeration.files.isNotEmpty()
            // The durable owner has the final word. A callback from a superseded epoch must not
            // publish a locally plausible inventory to a replacement UI/ledger session.
            val trusted = sessions.revalidate(epoch, candidateTrusted, !candidateTrusted) == SourceTrust.TRUSTED
            onReady(Observation(datalink, enumeration.files, enumeration.pagesEnded, enumeration.failed, trusted, started))
        }.start()
    }
}
