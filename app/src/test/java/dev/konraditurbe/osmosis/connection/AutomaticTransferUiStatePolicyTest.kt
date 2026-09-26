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
        assertEquals(AutomaticTransferUiStatePolicy.Phase.FINISHED,
            AutomaticTransferUiStatePolicy.project(null, "WRITER_COMPLETE")?.phase)
        assertNull(AutomaticTransferUiStatePolicy.project(null, "TRANSFER_REVIEW_REQUIRED"))
        assertNull(AutomaticTransferUiStatePolicy.project(null, "NOT_EVALUATED"))
    }

    @Test fun noWorkTerminalDecisionsClearTransientPreparationPresentation() {
        // The Activity hides its advisory progress area for a null projection.  These are
        // terminal scheduler outcomes, so retaining PLAN_LOOKUP would be misleading.
        assertNull(AutomaticTransferUiStatePolicy.project(null, "PLAN_NOT_ELIGIBLE"))
        assertNull(AutomaticTransferUiStatePolicy.project(null, "WRITER_ALREADY_ACTIVE"))
        assertNull(AutomaticTransferUiStatePolicy.project(null, "NO_LEDGER_SESSION"))
    }
}
