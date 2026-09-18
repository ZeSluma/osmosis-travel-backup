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
        if (!AutomaticTransferDispatchPolicy.mayStart(resources.automaticStrictTransferSupported, session, true, lease)) return
        ledger.automaticDownloadPaths(session, resources.trustedFilesByPath.values.toList()) { paths ->
            val current = sessions.snapshot()
            if (current.epoch != lease.epoch || !CameraSessionCoordinator.mayUseCameraTraffic(current)) return@automaticDownloadPaths
            val scheduled = runtime.plan(lease.epoch, SourceTrust.TRUSTED, ledger.latestPlan, current.userStopped)
            val backupLease = scheduled.first.lease ?: return@automaticDownloadPaths
            // A duplicate plan callback deliberately returns the already-current writer with no new
            // paths. Do nothing: completing it here would let the duplicate observer declare an
            // in-flight batch finished. Conversely, selected paths that no longer map to this
            // trusted observation are unsafe, not an empty successful batch.
            if (scheduled.second.isEmpty()) return@automaticDownloadPaths
            val selected = paths.intersect(scheduled.second)
            val jobs = selected.mapNotNull { resources.trustedFilesByPath[it] }.map { MediaDownloader.Job(it) }
            if (jobs.size != selected.size) {
                runtime.fail(backupLease, "TRANSFER_SOURCE_CHANGED")
                return@automaticDownloadPaths
            }
            val transferLease = sessions.acquireTransfer(lease.epoch) ?: run { runtime.fail(backupLease, "TRANSFER_BUSY_OR_UNTRUSTED"); return@automaticDownloadPaths }
            Thread {
                var failed = true
                try {
                    fun invalid(): Boolean = AutomaticTransferDispatchPolicy.shouldRefuse(
                        session, resources.ledgerSession, network, resources.transferNetwork, lease.epoch,
                        sessions.snapshot(), sessions.activeTransfer() == transferLease, runtime.accepts(backupLease),
                    )
                    val result = StrictTransferBatch.run(jobs, SilentProgress) { job, tick ->
                        if (invalid())
                            LedgerCoordinator.TransferResult.REVIEW_REQUIRED
                        else ledger.transferOriginal(session, job.file, network, ::invalid, tick)
                    }
                    // A pre-existing but unverified copy remains review-required. It cannot turn a
                    // durable automatic plan into a completed scheduler state.
                    failed = result.failed != 0 || result.existing != 0
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
