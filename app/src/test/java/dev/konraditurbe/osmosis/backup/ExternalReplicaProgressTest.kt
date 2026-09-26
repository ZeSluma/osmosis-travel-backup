package dev.konraditurbe.osmosis.backup

import org.junit.Assert.assertEquals
import org.junit.Test

class ExternalReplicaProgressTest {
    @Test fun externalProgressIsBoundedAndCountBased() {
        assertEquals(33, ExternalReplicaCoordinator.Progress(1, 3).percent)
        assertEquals(100, ExternalReplicaCoordinator.Progress(4, 3).percent)
        assertEquals(0, ExternalReplicaCoordinator.Progress(0, 0).percent)
    }
}
