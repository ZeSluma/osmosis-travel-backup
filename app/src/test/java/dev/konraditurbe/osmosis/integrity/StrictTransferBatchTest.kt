package dev.konraditurbe.osmosis.integrity

import dev.konraditurbe.osmosis.core.CameraFile
import dev.konraditurbe.osmosis.ledger.LedgerCoordinator.TransferResult
import dev.konraditurbe.osmosis.net.MediaDownloader
import org.junit.Assert.*
import org.junit.Test

class StrictTransferBatchTest {
    private class Events:MediaDownloader.Progress {
        val done=mutableListOf<Boolean>();var counts=Triple(-1,-1,-1)
        override fun onStart(totalFiles:Int,totalBytes:Long){}
        override fun onFileStart(index:Int,name:String,fileBytes:Long){}
        override fun onTick(fileDone:Long,overallDone:Long){}
        override fun onFileDone(index:Int,done:Boolean){this.done+=done}
        override fun onComplete(saved:Int,skipped:Int,failed:Int){counts=Triple(saved,skipped,failed)}
    }
    private fun job(name:String="test.MP4")=MediaDownloader.Job(CameraFile(name,"",sizeBytes=100))
    @Test fun existingUnverifiedIsPreservedWithoutPretendingCompletion(){
        val p=Events()
        StrictTransferBatch.run(listOf(job()),p){_,_->TransferResult.EXISTING_UNVERIFIED}
        assertEquals(listOf(false),p.done);assertEquals(Triple(0,1,0),p.counts)
    }
    @Test fun failuresCannotDisappearFromQueue(){
        val p=Events()
        StrictTransferBatch.run(listOf(job(),job()),p){_,_->throw IllegalStateException()}
        assertEquals(listOf(false,false),p.done);assertEquals(Triple(0,0,2),p.counts)
    }
    @Test fun returnedSummaryLetsSchedulerKeepPartialWorkActionable(){
        val p=Events()
        val result=StrictTransferBatch.run(listOf(job(),job()),p){_,_->TransferResult.REVIEW_REQUIRED}
        assertEquals(0,result.saved);assertEquals(0,result.existing);assertEquals(2,result.failed)
    }
    @Test fun unsupportedAssetNeverReachesTransferAdapter(){
        val p=Events()
        StrictTransferBatch.run(listOf(job("test.DNG")),p){_,_->error("must not call")}
        assertEquals(Triple(0,0,1),p.counts);assertEquals(listOf(false),p.done)
    }
    @Test fun onlyCompletedTransferLeavesQueueAndRemainsUnverified(){
        val p=Events()
        StrictTransferBatch.run(listOf(job()),p){_,tick->tick(100);TransferResult.TRANSFERRED_UNVERIFIED}
        assertEquals(listOf(true),p.done);assertEquals(Triple(1,0,0),p.counts)
    }
}
