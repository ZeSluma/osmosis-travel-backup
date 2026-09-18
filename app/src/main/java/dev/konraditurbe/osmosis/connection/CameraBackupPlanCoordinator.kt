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
        resources.trustedFilesByPath = if (sourceTrusted) files.associateBy { it.path } else emptyMap()
        ledger.observe(association, session, files, enumerationComplete, !sourceTrusted,
            sourceTrusted, sourceTrusted, sourceTrusted, started) { planned ->
            if (planned) dispatcher.dispatch()
            onProjectionChanged()
        }
    }
}
