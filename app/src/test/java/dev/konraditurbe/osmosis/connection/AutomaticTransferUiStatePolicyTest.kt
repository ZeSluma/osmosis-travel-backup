package dev.konraditurbe.osmosis.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutomaticTransferUiStatePolicyTest {
    @Test fun serviceProgressDrivesOnlyTheTransferStage() {
        assertEquals(AutomaticTransferUiStatePolicy.State(AutomaticTransferUiStatePolicy.Phase.TRANSFERRING, 37, 2),
            AutomaticTransferUiStatePolicy.project("transfer=37% (1/2)", "WRITER_STARTED"))
    }

    @Test fun terminalAndUnsafeServiceDecisionsStayDistinct() {
        assertEquals(AutomaticTransferUiStatePolicy.Phase.PREPARING,
            AutomaticTransferUiStatePolicy.project(null, "PLAN_LOOKUP")?.phase)
        assertEquals(AutomaticTransferUiStatePolicy.Phase.WAITING_FOR_WRITER,
            AutomaticTransferUiStatePolicy.project(null, "WRITER_WAIT")?.phase)
        val finished = AutomaticTransferUiStatePolicy.project(null, "WRITER_COMPLETE")
        assertEquals(AutomaticTransferUiStatePolicy.Phase.FINISHED, finished?.phase)
        assertEquals(3_000L, finished?.dismissAfterMs)
        assertNull(AutomaticTransferUiStatePolicy.project(null, "TRANSFER_REVIEW_REQUIRED"))
        assertNull(AutomaticTransferUiStatePolicy.project(null, "NOT_EVALUATED"))
    }

    @Test fun noWorkTerminalDecisionsClearTransientPreparationPresentation() {
        // The Activity hides its advisory progress area for a null projection.  These are
        // terminal scheduler outcomes, so retaining PLAN_LOOKUP would be misleading.
        assertNull(AutomaticTransferUiStatePolicy.project(null, "PLAN_NOT_ELIGIBLE"))
        assertNull(AutomaticTransferUiStatePolicy.project(null, "WRITER_ALREADY_ACTIVE"))
        assertNull(AutomaticTransferUiStatePolicy.project(null, "NO_LEDGER_SESSION"))
        assertNull(AutomaticTransferUiStatePolicy.project(null, "SOURCE_CHANGED"))
    }

    @Test fun everyDispatcherDecisionHasAnExplicitActiveOrTerminalPresentation() {
        val active = mapOf(
            "PLAN_LOOKUP" to AutomaticTransferUiStatePolicy.Phase.PREPARING,
            "WRITER_WAIT" to AutomaticTransferUiStatePolicy.Phase.WAITING_FOR_WRITER,
            "WRITER_COMPLETE" to AutomaticTransferUiStatePolicy.Phase.FINISHED,
        )
        active.forEach { (decision, phase) ->
            assertEquals(phase, AutomaticTransferUiStatePolicy.project(null, decision)?.phase)
        }
        listOf(
            "NOT_EVALUATED", "NO_LEDGER_SESSION", "NO_CAMERA_NETWORK", "USER_STOPPED",
            "STRICT_TRANSFER_UNSUPPORTED", "SESSION_NOT_READY", "STALE_OR_UNTRUSTED",
            "PLAN_NOT_ELIGIBLE", "WRITER_ALREADY_ACTIVE", "SOURCE_CHANGED",
            "TRANSFER_REVIEW_REQUIRED",
        ).forEach { decision -> assertNull(AutomaticTransferUiStatePolicy.project(null, decision)) }
    }

    @Test fun transferProgressIsBoundedAndDoesNotDependOnAStaleDecision() {
        assertEquals(0, AutomaticTransferUiStatePolicy.project("transfer=0% (0/1)", "WRITER_STARTED")?.percent)
        assertNull(AutomaticTransferUiStatePolicy.project("transfer=100% (1/1)", "SOURCE_CHANGED"))
        assertEquals(AutomaticTransferUiStatePolicy.Phase.FINISHED,
            AutomaticTransferUiStatePolicy.project("transfer=100% (1/1)", "WRITER_COMPLETE")?.phase)
    }
}
