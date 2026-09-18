package dev.konraditurbe.osmosis.backup

import dev.konraditurbe.osmosis.connection.*
import dev.konraditurbe.osmosis.ledger.*
import org.junit.Assert.*
import org.junit.Test

/** Cross-layer deterministic fault scenario; fake inputs are not Pocket/SSD hardware evidence. */
class EndToEndBackupRecoveryScenarioTest {
    private val complete=PlanResult("snapshot",listOf(PlanItem("asset",PlanAction.DOWNLOAD,"2026-09-18/a.mp4")),true,true,true)
    @Test fun lossReplacementStaleCallbackRevalidationAndReplicationStayFailClosed(){
        var session=CameraSessionCoordinator.begin(SessionLease())
        val epoch=session.epoch
        session=CameraSessionCoordinator.event(session,epoch,ConnectionEvent.TRANSPORT_READY)
        session=CameraSessionCoordinator.event(session,epoch,ConnectionEvent.REVALIDATED)
        assertTrue(CameraSessionCoordinator.mayUseCameraTraffic(session))
        val runtime=AutonomousBackupRuntime()
        val old=runtime.plan(epoch,SourceTrust.TRUSTED,complete,false).first.lease!!
        // Active transfer loses transport. A replacement session invalidates old completion.
        session=CameraSessionCoordinator.event(session,epoch,ConnectionEvent.LOST,ConnectionReason.NETWORK_LOSS)
        session=CameraSessionCoordinator.event(session,epoch,ConnectionEvent.RETRY_TIMER)
        val replacement=CameraSessionCoordinator.begin(session)
        val new=runtime.plan(replacement.epoch,SourceTrust.TRUSTED,complete,false).first.lease!!
        assertFalse(runtime.complete(old));assertTrue(runtime.accepts(new))
        // Replacement transport cannot transfer after an incomplete source observation.
        assertTrue(runtime.plan(replacement.epoch,SourceTrust.INCOMPLETE_UNTRUSTED,complete,false).second.isEmpty())
        // A new trusted observation is required before replication; absence and stop both win.
        val phone=mapOf("asset" to ReplicaStatus(ReplicaState.VERIFIED,ReplicaProof(1,"a".repeat(64))))
        assertTrue(runtime.replication(replacement.epoch,phone,false,false).second.isEmpty())
        assertTrue(runtime.replication(replacement.epoch,phone,true,true).second.isEmpty())
        assertEquals(BackupPhase.STOPPED,runtime.snapshot().phase)
    }
}
