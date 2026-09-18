package dev.konraditurbe.osmosis.ledger

import org.junit.Assert.assertEquals
import org.junit.Test

class AutomaticBackupPlanTest {
    private val items=listOf(PlanItem("new",PlanAction.DOWNLOAD,"2026-09-17/new.mp4"),
        PlanItem("partial",PlanAction.RESUME_REVALIDATE,"2026-09-17/partial.mp4"),
        PlanItem("unknown",PlanAction.REVIEW_UNKNOWN,"2026-09-17/unknown.bin"))
    @Test fun incompleteInventoryQueuesNothing() {
        assertEquals(emptySet<String>(),AutomaticBackupPlan.downloadAssetIds(PlanResult("s",items,false,false,false)))
    }
    @Test fun completeInventoryQueuesOnlyNewSafeDownloadWork() {
        assertEquals(setOf("new"),AutomaticBackupPlan.downloadAssetIds(PlanResult("s",items,true,false,false)))
    }
}
