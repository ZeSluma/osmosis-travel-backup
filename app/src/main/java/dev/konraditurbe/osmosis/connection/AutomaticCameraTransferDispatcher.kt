package dev.konraditurbe.osmosis.connection

import android.content.Context
import dev.konraditurbe.osmosis.backup.AutonomousBackupRuntime
import dev.konraditurbe.osmosis.integrity.StrictTransferBatch
import dev.konraditurbe.osmosis.ledger.LedgerCoordinator
import dev.konraditurbe.osmosis.net.MediaDownloader
import dev.konraditurbe.osmosis.core.DiagnosticEventStore

/** Service-owned automatic strict-transfer effect. UI may observe ledger state but never owns this writer. */
class AutomaticCameraTransferDispatcher(
    private val context: Context,
    private val resources: CameraSessionResources,
    private val sessions: DurableSessionRuntime,
    private val runtime: AutonomousBackupRuntime,
    private val ledger: LedgerCoordinator,
) {
    /** Privacy-safe service diagnostic; contains no asset, path, network or credential data. */
    @Volatile var lastDecision: String = "NOT_EVALUATED"
        private set
    @Volatile var progress: String? = null
        private set
    /** Live cell overlays, keyed by the same exact display identity the grid uses. */
    @Volatile var fileProgress: Map<String, LiveTransferFileProjection> = emptyMap()
        private set
    @Volatile private var progressPercent: Long = -1L
    @Volatile private var waitingForWriterEpoch: Long? = null
    init {
        sessions.observeTransferRelease { onTransferReleased() }
    }
    private fun decision(value: String) {
        lastDecision = value
        DiagnosticEventStore.open(context).record(DiagnosticEventStore.Type.TRANSFER_STATE, newState = value)
        // Publish every state transition, including planning/writer-wait. The UI remains an
        // observer and may show honest progress before bytes begin moving.
        CameraConnectionService.backupProjectionNotifier(context).publish()
    }
    fun dispatch() {
        val session = resources.ledgerSession ?: run { decision("NO_LEDGER_SESSION"); return }
        val network = resources.transferNetwork ?: run { decision("NO_CAMERA_NETWORK"); return }
        val lease = sessions.snapshot()
        if (!AutomaticTransferDispatchPolicy.mayStart(resources.automaticStrictTransferSupported, session, true, lease)) {
            decision(if (lease.userStopped) "USER_STOPPED" else if (!resources.automaticStrictTransferSupported) "STRICT_TRANSFER_UNSUPPORTED" else "SESSION_NOT_READY")
            return
        }
        decision("PLAN_LOOKUP")
        ledger.automaticDownloadPaths(session, resources.trustedFilesByPath.values.toList()) { files ->
            val current = sessions.snapshot()
            if (current.epoch != lease.epoch || !CameraSessionCoordinator.mayUseCameraTraffic(current)) { decision("STALE_OR_UNTRUSTED"); return@automaticDownloadPaths }
            // Take the single camera writer before allocating a backup lease.  A replacement
            // epoch may arrive while the old writer is cancelling; treating that as a plan failure
            // would strand safe automatic continuation in USER_ACTION_REQUIRED.
            val transferLease = sessions.acquireTransfer(lease.epoch) ?: run {
                waitingForWriterEpoch = lease.epoch
                decision("WRITER_WAIT")
                return@automaticDownloadPaths
            }
            waitingForWriterEpoch = null
            val scheduled = runtime.plan(lease.epoch, SourceTrust.TRUSTED, ledger.latestPlan, current.userStopped)
            val backupLease = scheduled.first.lease ?: run {
                sessions.releaseTransfer(transferLease)
                decision("PLAN_NOT_ELIGIBLE")
                return@automaticDownloadPaths
            }
            // A duplicate plan callback deliberately returns the already-current writer with no new
            // paths. Do nothing: completing it here would let the duplicate observer declare an
            // in-flight batch finished. Conversely, selected paths that no longer map to this
            // trusted observation are unsafe, not an empty successful batch.
            if (scheduled.second.isEmpty()) {
                sessions.releaseTransfer(transferLease)
                decision("WRITER_ALREADY_ACTIVE")
                return@automaticDownloadPaths
            }
            val selected = files.keys.intersect(scheduled.second)
            if (!AutomaticTransferDispatchPolicy.hasEveryPlannedSource(scheduled.second, selected)) {
                runtime.fail(backupLease, "TRANSFER_SOURCE_CHANGED")
                sessions.releaseTransfer(transferLease)
                decision("SOURCE_CHANGED")
                return@automaticDownloadPaths
            }
            val jobs = selected.mapNotNull { files[it] }.map { MediaDownloader.Job(it) }
            if (jobs.size != selected.size) {
                runtime.fail(backupLease, "TRANSFER_SOURCE_CHANGED")
                sessions.releaseTransfer(transferLease)
                // Do not leave PLAN_LOOKUP visible when the trusted observation changed while
                // converting the plan into jobs.  This is a fail-closed review outcome, not an
                // indefinitely running preparation state.
                decision("SOURCE_CHANGED")
                return@automaticDownloadPaths
            }
            decision("WRITER_STARTED")
            progress = AutomaticTransferProgressPolicy.project(jobs.size, jobs.sumOf { it.file.sizeBytes.coerceAtLeast(0L) }, 0, 0).text
            progressPercent = -1L
            publishProgress()
            Thread {
                var failed = true
                try {
                    fun invalid(): Boolean = AutomaticTransferDispatchPolicy.shouldRefuse(
                        session, resources.ledgerSession, network, resources.transferNetwork, lease.epoch,
                        sessions.snapshot(), sessions.activeTransfer() == transferLease, runtime.accepts(backupLease),
                    )
                    val result = StrictTransferBatch.run(jobs, progressReporter(jobs)) { job, tick ->
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
                    if (!failed) {
                        runtime.complete(backupLease)
                        decision("WRITER_COMPLETE")
                    } else {
                        runtime.fail(backupLease, "TRANSFER_REVIEW_REQUIRED")
                        decision("TRANSFER_REVIEW_REQUIRED")
                    }
                    // Completion/failure is recorded before a waiting replacement can acquire the
                    // writer.  Therefore a late predecessor cannot overwrite the replacement's
                    // scheduler state after it is woken.
                    sessions.releaseTransfer(transferLease)
                    progress = null
                    progressPercent = -1L
                    // The final durable reread must replace every transient cell overlay. A
                    // completed or failed file may never remain visually active after its writer
                    // has released.
                    fileProgress = LiveTransferFileProjectionPolicy.clearAtTerminal()
                    // The Activity observes this service signal and re-reads durable receipt state;
                    // it never receives transfer truth directly from the worker.
                    CameraConnectionService.backupProjectionNotifier(context).publish()
                }
            }.start()
        }
    }
    private fun progressReporter(jobs: List<MediaDownloader.Job>) = object : MediaDownloader.Progress {
        private var totalFiles = jobs.size
        private var totalBytes = jobs.sumOf { it.file.sizeBytes.coerceAtLeast(0L) }
        private var completedFiles = 0
        private var lastOverallDone = 0L
        override fun onStart(totalFiles: Int, totalBytes: Long) {
            this.totalFiles = totalFiles
            this.totalBytes = totalBytes
            update(0L)
        }
        // Names are intentionally ignored: the observer diagnostic must not expose media metadata.
        override fun onFileStart(index: Int, name: String, fileBytes: Long) {
            updateFile(index, LiveTransferFileProjectionPolicy.downloading(0L, fileBytes))
        }
        override fun onTick(fileDone: Long, overallDone: Long) {
            lastOverallDone = overallDone.coerceAtLeast(lastOverallDone)
            val index = activeIndex.coerceIn(0, jobs.lastIndex)
            updateFile(index, LiveTransferFileProjectionPolicy.downloading(fileDone, jobs[index].file.sizeBytes))
            update(lastOverallDone)
        }
        override fun onFileDone(index: Int, done: Boolean) {
            if (done) completedFiles++
            updateFile(index, LiveTransferFileProjectionPolicy.completed(done))
            update(lastOverallDone)
        }
        override fun onComplete(saved: Int, skipped: Int, failed: Int) = Unit
        private fun update(overallDone: Long) {
            val next = AutomaticTransferProgressPolicy.project(totalFiles, totalBytes, completedFiles, overallDone)
            if (AutomaticTransferProgressPolicy.shouldPublish(progressPercent, next.percent)) {
                progress = next.text
                progressPercent = next.percent
                publishProgress()
            }
        }
        private var activeIndex = 0
        private fun updateFile(index: Int, projection: LiveTransferFileProjection) {
            activeIndex = index
            val key = dev.konraditurbe.osmosis.ledger.LedgerCoordinator.displayKey(jobs[index].file)
            fileProgress = LiveTransferFileProjectionPolicy.update(fileProgress, key, projection)
            publishProgress()
        }
    }

    private fun publishProgress() = CameraConnectionService.backupProjectionNotifier(context).publish()

    private fun onTransferReleased() {
        val expected = waitingForWriterEpoch ?: return
        val current = sessions.snapshot()
        if (expected != current.epoch || !CameraSessionCoordinator.mayUseCameraTraffic(current)) {
            waitingForWriterEpoch = null
            return
        }
        // Only the release of the single runtime allocation wakes this deferred dispatch.  The
        // next dispatch still rechecks epoch, trust, durable plan and every source identity.
        waitingForWriterEpoch = null
        dispatch()
    }
}
