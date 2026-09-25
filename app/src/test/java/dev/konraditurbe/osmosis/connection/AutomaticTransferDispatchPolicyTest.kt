package dev.konraditurbe.osmosis.connection

import dev.konraditurbe.osmosis.backup.AutonomousBackupRuntime
import dev.konraditurbe.osmosis.ledger.PlanAction
import dev.konraditurbe.osmosis.ledger.PlanItem
import dev.konraditurbe.osmosis.ledger.PlanResult
import org.junit.Assert.*
import org.junit.Test

class AutomaticTransferDispatchPolicyTest {
    private fun ready(epoch: Long = 7) = SessionLease(epoch, RecoverySnapshot(ConnectionState.READY))
    @Test fun onlyTrustedLiveServiceStateMayStartAutomaticWriter() {
        assertTrue(AutomaticTransferDispatchPolicy.mayStart(true, "ledger", true, ready()))
        assertFalse(AutomaticTransferDispatchPolicy.mayStart(false, "ledger", true, ready()))
        assertFalse(AutomaticTransferDispatchPolicy.mayStart(true, null, true, ready()))
        assertFalse(AutomaticTransferDispatchPolicy.mayStart(true, "ledger", false, ready()))
        assertFalse(AutomaticTransferDispatchPolicy.mayStart(true, "ledger", true, ready().copy(userStopped = true)))
    }
    @Test fun anyReplacementOrLossRefusesInFlightAutomaticWriter() {
        val network = Any()
        fun refused(session: String? = "ledger", actualNetwork: Any? = network, epoch: Long = 7, transfer: Boolean = true, scheduler: Boolean = true) =
            AutomaticTransferDispatchPolicy.shouldRefuse("ledger", session, network, actualNetwork, 7, ready(epoch), transfer, scheduler)
        assertFalse(refused())
        assertTrue(refused(session = "replacement"))
        assertTrue(refused(actualNetwork = Any()))
        assertTrue(refused(epoch = 8))
        assertTrue(refused(transfer = false))
        assertTrue(refused(scheduler = false))
        assertTrue(AutomaticTransferDispatchPolicy.shouldRefuse("ledger", "ledger", network, network, 7,
            ready().copy(recovery = RecoverySnapshot(ConnectionState.REVALIDATING)), true, true))
    }
    @Test fun restoredOrRecoveringSessionCannotDispatchUntilFreshRevalidation() {
        var session = CameraSessionCoordinator.begin(SessionLease())
        val epoch = session.epoch
        session = CameraSessionCoordinator.event(session, epoch, ConnectionEvent.TRANSPORT_READY)
        session = CameraSessionCoordinator.event(session, epoch, ConnectionEvent.REVALIDATED)
        assertTrue(AutomaticTransferDispatchPolicy.mayStart(true, "ledger", true, session))
        session = CameraSessionCoordinator.event(session, epoch, ConnectionEvent.LOST, ConnectionReason.NETWORK_LOSS)
        session = CameraSessionCoordinator.event(session, epoch, ConnectionEvent.RETRY_TIMER)
        assertFalse(AutomaticTransferDispatchPolicy.mayStart(true, "ledger", true, session))
        session = CameraSessionCoordinator.event(session, epoch, ConnectionEvent.TRANSPORT_READY)
        assertFalse(AutomaticTransferDispatchPolicy.mayStart(true, "ledger", true, session))
        session = CameraSessionCoordinator.event(session, epoch, ConnectionEvent.REVALIDATED)
        assertTrue(AutomaticTransferDispatchPolicy.mayStart(true, "ledger", true, session))
    }
    @Test fun duplicatePlanPublicationDoesNotAllocateOrCompleteTheCurrentWriter() {
        val plan = PlanResult("snapshot", listOf(PlanItem("asset", PlanAction.DOWNLOAD, "day/asset.mp4")),
            enumerationComplete = true, recordingComplete = true, localComplete = true)
        val runtime = AutonomousBackupRuntime()
        val first = runtime.plan(7, SourceTrust.TRUSTED, plan, userStopped = false)
        val second = runtime.plan(7, SourceTrust.TRUSTED, plan, userStopped = false)
        assertEquals(first.first.lease, second.first.lease)
        assertTrue(second.second.isEmpty())
        assertTrue(runtime.accepts(checkNotNull(first.first.lease)))
    }
    @Test fun partialOrEmptyLiveSourceProjectionCannotCompleteAPlannedBatch() {
        val planned = setOf("one", "two")
        assertFalse(AutomaticTransferDispatchPolicy.hasEveryPlannedSource(planned, emptySet()))
        assertFalse(AutomaticTransferDispatchPolicy.hasEveryPlannedSource(planned, setOf("one")))
        assertTrue(AutomaticTransferDispatchPolicy.hasEveryPlannedSource(planned, setOf("one", "two", "extra")))
    }
}
