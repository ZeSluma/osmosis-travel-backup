package dev.konraditurbe.osmosis.connection

import org.junit.Assert.*
import org.junit.Test

class AutomaticCameraAvailabilityTest {
    @Test fun mostRecentKnownAdvertisedCameraIsSelected(){
        assertEquals("B",AutomaticCameraAvailability.select(listOf("A","B"),setOf("B","C"),false,false))
    }
    @Test fun userStopAndExistingConnectionSuppressAutomaticReconnect(){
        assertNull(AutomaticCameraAvailability.select(listOf("A"),setOf("A"),true,false))
        assertNull(AutomaticCameraAvailability.select(listOf("A"),setOf("A"),false,true))
    }
}
