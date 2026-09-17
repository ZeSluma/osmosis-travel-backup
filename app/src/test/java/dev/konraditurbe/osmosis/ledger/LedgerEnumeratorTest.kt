package dev.konraditurbe.osmosis.ledger

import dev.konraditurbe.osmosis.core.CameraFile
import org.junit.Assert.*
import org.junit.Test

class LedgerEnumeratorTest {
    private fun file(n: String) = CameraFile("DCIM/$n", "",sizeBytes=100)
    @Test fun emptySourceCompletesEnumerationOnly() { val r=LedgerEnumerator.enumerate({emptyList()},{false},{error("unexpected")}); assertTrue(r.pagesEnded);assertTrue(r.files.isEmpty());assertFalse(r.failed) }
    @Test fun multiplePagesAccumulateAutomatically() {
        var page=0
        val r=LedgerEnumerator.enumerate({listOf(file("a.mp4"))},{page<2},{page++;listOf(file("$page.mp4"))})
        assertEquals(3,r.files.size);assertTrue(r.pagesEnded)
    }
    @Test fun repeatedPageFailsWithoutLosingDiscoveredAssets() { val r=LedgerEnumerator.enumerate({listOf(file("a"))},{true},{listOf(file("a"))});assertEquals(1,r.files.size);assertTrue(r.failed);assertFalse(r.pagesEnded) }
    @Test fun halfwayExceptionRetainsPartialInventory() { val r=LedgerEnumerator.enumerate({listOf(file("a"))},{true},{error("synthetic")});assertEquals(1,r.files.size);assertTrue(r.failed);assertFalse(r.pagesEnded) }
    @Test fun changedMetadataCannotSealInventory() { val r=LedgerEnumerator.enumerate({listOf(file("a"))},{true},{listOf(file("a").copy(sizeBytes=101))});assertTrue(r.failed);assertFalse(r.pagesEnded) }
    @Test fun pageLimitPreventsUnboundedScan() { var i=0;val r=LedgerEnumerator.enumerate({listOf(file("a"))},{true},{listOf(file("${++i}"))},2);assertTrue(r.failed);assertEquals(2,r.files.size) }
    @Test fun differentStoresDoNotCollapse() { val r=LedgerEnumerator.enumerate({listOf(file("a"),file("a").copy(storage=1))},{false},{emptyList()});assertEquals(2,r.files.size) }
    @Test fun originalVideoPhotoRawAudioIncludedUnknownPreserved() {
        for (ext in listOf("MP4","MOV","JPG","DNG","RAW","WAV")) assertEquals(AssetClass.KNOWN_REQUIRED,CameraLedgerAdapter.asset(file("a.$ext")).classification)
        assertEquals(AssetClass.UNKNOWN_POTENTIALLY_REQUIRED,CameraLedgerAdapter.asset(file("a.XYZ")).classification)
        assertEquals(AssetClass.UNKNOWN_POTENTIALLY_REQUIRED,CameraLedgerAdapter.asset(file("a.LRF")).classification)
    }
    @Test fun cameraAdapterNeverTrustsFilenameTimeOrInventsCompanions() { val a=CameraLedgerAdapter.asset(file("DJI_20261115010101_0001_D.MP4"));assertTrue(a.capture.isEmpty());assertFalse(a.membersComplete);assertNull(a.recordingKey);assertNull(a.strongVersion) }
}
