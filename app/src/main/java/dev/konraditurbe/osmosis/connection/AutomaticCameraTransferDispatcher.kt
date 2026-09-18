package dev.konraditurbe.osmosis.connection

import android.content.Context
import dev.konraditurbe.osmosis.backup.AutonomousBackupRuntime
import dev.konraditurbe.osmosis.integrity.StrictTransferBatch
import dev.konraditurbe.osmosis.ledger.LedgerCoordinator
import dev.konraditurbe.osmosis.net.MediaDownloader

/** Service-owned automatic strict-transfer effect. UI may observe ledger state but never owns this writer. */
class AutomaticCameraTransferDispatcher(
    private val context: Context,
    private val resources: CameraSessionResources,
    private val sessions: DurableSessionRuntime,
    private val runtime: AutonomousBackupRuntime,
    private val ledger: LedgerCoordinator,
) {
    fun dispatch() {
        val session = resources.ledgerSession ?: return
        val network = resources.transferNetwork ?: return
        val lease = sessions.snapshot()
        if (!resources.automaticStrictTransferSupported || !CameraSessionCoordinator.mayUseCameraTraffic(lease)) return
        ledger.automaticDownloadPaths(session, resources.trustedFilesByPath.values.toList()) { paths ->
            val current = sessions.snapshot()
            if (current.epoch != lease.epoch || !CameraSessionCoordinator.mayUseCameraTraffic(current)) return@automaticDownloadPaths
            val scheduled = runtime.plan(lease.epoch, SourceTrust.TRUSTED, ledger.latestPlan, current.userStopped)
            val backupLease = scheduled.first.lease ?: return@automaticDownloadPaths
            val jobs = paths.intersect(scheduled.second).mapNotNull { resources.trustedFilesByPath[it] }.map { MediaDownloader.Job(it) }
            if (jobs.isEmpty()) { runtime.complete(backupLease); return@automaticDownloadPaths }
            val transferLease = sessions.acquireTransfer(lease.epoch) ?: run { runtime.fail(backupLease, "TRANSFER_BUSY_OR_UNTRUSTED"); return@automaticDownloadPaths }
            Thread {
                var failed = true
                try {
                    fun invalid(): Boolean = resources.ledgerSession != session || resources.transferNetwork != network ||
                        sessions.snapshot().epoch != lease.epoch || sessions.activeTransfer() != transferLease ||
                        !runtime.accepts(backupLease) || !CameraSessionCoordinator.mayUseCameraTraffic(sessions.snapshot())
                    val result = StrictTransferBatch.run(jobs, SilentProgress) { job, tick ->
                        if (invalid())
                            LedgerCoordinator.TransferResult.REVIEW_REQUIRED
                        else ledger.transferOriginal(session, job.file, network, ::invalid, tick)
                    }
                    failed = result.failed != 0
                } catch (_: Exception) {
                    failed = true
                } finally {
                    sessions.releaseTransfer(transferLease)
                    if (!failed) runtime.complete(backupLease) else runtime.fail(backupLease, "TRANSFER_REVIEW_REQUIRED")
                }
            }.start()
        }
    }
    private object SilentProgress : MediaDownloader.Progress {
        override fun onStart(totalFiles: Int, totalBytes: Long) = Unit
        override fun onFileStart(index: Int, name: String, fileBytes: Long) = Unit
        override fun onTick(fileDone: Long, overallDone: Long) = Unit
        override fun onFileDone(index: Int, done: Boolean) = Unit
        override fun onComplete(saved: Int, skipped: Int, failed: Int) = Unit
    }
}
