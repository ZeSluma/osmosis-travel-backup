package dev.konraditurbe.osmosis.connection

import dev.konraditurbe.osmosis.core.CameraFile
import dev.konraditurbe.osmosis.ledger.LedgerCoordinator
import java.time.Instant

/** Service-layer bridge from a trusted source observation to durable planning and automatic dispatch. */
class CameraBackupPlanCoordinator(
    private val resources: CameraSessionResources,
    private val ledger: LedgerCoordinator,
    private val dispatcher: AutomaticCameraTransferDispatcher,
) {
    fun publish(
        association: String,
        files: List<CameraFile>,
        enumerationComplete: Boolean,
        sourceTrusted: Boolean,
        started: Instant,
        onProjectionChanged: () -> Unit,
    ) {
        val session = ledger.newSession()
        resources.ledgerSession = session
        resources.trustedFilesByPath = emptyMap()
        resources.recordSourcePage(session, files)
        ledger.observe(association, session, files, enumerationComplete, !sourceTrusted,
            sourceTrusted, sourceTrusted, sourceTrusted, started) { planned ->
            // `planned` is a projection hint only. The durable dispatcher rechecks trust,
            // current-source eligibility and writer fencing itself; coupling its wake-up to this
            // second callback can strand a safe persisted DOWNLOAD plan.
            dispatcher.dispatch()
            onProjectionChanged()
        }
    }

    /**
     * A UI may request another manifest page, but it must hand the result back to this service
     * bridge. The bridge keeps one durable session, aggregates only that session's pages and
     * starts automatic work only after the terminal trusted observation is planned.
     */
    fun append(
        session: String,
        association: String,
        files: List<CameraFile>,
        enumerationComplete: Boolean,
        sourceTrusted: Boolean,
        started: Instant,
        onProjectionChanged: () -> Unit,
    ) {
        if (!resources.acceptsSourcePage(session, association) || !resources.recordSourcePage(session, files)) return
        ledger.observe(association, session, files, enumerationComplete, !sourceTrusted,
            sourceTrusted, sourceTrusted, sourceTrusted, started) { planned ->
            dispatcher.dispatch()
            onProjectionChanged()
        }
    }
}
