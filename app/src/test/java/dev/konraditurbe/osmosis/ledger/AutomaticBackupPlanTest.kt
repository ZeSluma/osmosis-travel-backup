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
    @Test fun historicalAmbiguityBlocksCompletionButNotFreshSafeDownloads() {
        val plan = PlanResult("s", items, enumerationComplete = false, recordingComplete = false,
            localComplete = false, automaticDownloadEligible = true)
        assertEquals(setOf("new"), AutomaticBackupPlan.downloadAssetIds(plan))
    }
    @Test fun planActionCountsRemainSeparatedForSanitizedDiagnosis() {
        val plan=PlanResult("s",items,true,false,false)
        assertEquals(1,plan.items.count { it.action==PlanAction.DOWNLOAD })
        assertEquals(1,plan.items.count { it.action==PlanAction.RESUME_REVALIDATE })
        assertEquals(1,plan.items.count { it.action==PlanAction.REVIEW_UNKNOWN })
    }
}
