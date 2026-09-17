package dev.konraditurbe.osmosis.integrity

import dev.konraditurbe.osmosis.ledger.LedgerCoordinator.TransferResult
import dev.konraditurbe.osmosis.net.MediaDownloader

/** Explicit UI jobs only; no legacy fallback or autonomous retries for the strict Pocket path. */
object StrictTransferBatch {
    fun run(jobs:List<MediaDownloader.Job>, progress:MediaDownloader.Progress,
        transfer:(MediaDownloader.Job,(Long)->Unit)->TransferResult) {
        progress.onStart(jobs.size,jobs.sumOf{it.file.sizeBytes.coerceAtLeast(0)})
        var saved=0;var existing=0;var failed=0;var completedBytes=0L
        jobs.forEachIndexed { index,job ->
            progress.onFileStart(index,job.file.name,job.file.sizeBytes.coerceAtLeast(0))
            val result=try {
                if(job.trim!=null || job.file.ext !in setOf("MP4","MOV")) TransferResult.REVIEW_REQUIRED
                else transfer(job){bytes->progress.onTick(bytes,completedBytes+bytes)}
            }catch(_:Exception){TransferResult.REVIEW_REQUIRED}
            when(result){
                TransferResult.TRANSFERRED_UNVERIFIED->{saved++;completedBytes+=job.file.sizeBytes.coerceAtLeast(0)}
                TransferResult.EXISTING_UNVERIFIED->existing++
                TransferResult.REVIEW_REQUIRED->failed++
            }
            // Existing unverified copies stay visible in the queue for explicit review, never redownload.
            progress.onFileDone(index,result==TransferResult.TRANSFERRED_UNVERIFIED)
        }
        progress.onComplete(saved,existing,failed)
    }
}
