package dev.konraditurbe.osmosis.connection

import android.app.Instrumentation
import android.content.Intent
import dev.konraditurbe.osmosis.ui.MainActivity

/** Emulator-only lifecycle proof; it never requests BLE, Wi-Fi, camera media, or a transfer. */
object Gate7LifecycleInstrumentation {
    fun run(instrumentation: Instrumentation, phase: String): String {
        check(android.os.Build.HARDWARE == "ranchu" && android.os.Build.MODEL.contains("sdk", true))
        val context = instrumentation.targetContext
        check(context.getSharedPreferences("osmosis", 0).all.isEmpty())
        return when (phase) {
            "recreate" -> recreationAndBackground(instrumentation, context)
            "processRestore" -> processRestore(context)
            else -> error("unknown lifecycle phase")
        }
    }

    private fun recreationAndBackground(instrumentation: Instrumentation, context: android.content.Context): String {
        val runtime = CameraConnectionService.runtime(context)
        val lease = runtime.start()
        runtime.callback(lease.epoch, ConnectionEvent.TRANSPORT_READY)
        check(runtime.revalidated(lease.epoch, SourceObservation(true, false)) == SourceTrust.TRUSTED)
        val transfer = checkNotNull(runtime.acquireTransfer(lease.epoch))
        var activity = instrumentation.startActivitySync(Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN; addCategory(Intent.CATEGORY_LAUNCHER); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        instrumentation.waitForIdleSync()
        instrumentation.runOnMainSync { activity.recreate() }
        instrumentation.waitForIdleSync()
        // Recreate must attach to, not replace, the service-owned active session/writer.
        check(runtime.snapshot().epoch == lease.epoch)
        check(runtime.activeTransfer() == transfer)
        instrumentation.runOnMainSync { activity.moveTaskToBack(true) }
        instrumentation.waitForIdleSync()
        check(runtime.snapshot().epoch == lease.epoch)
        check(runtime.activeTransfer() == transfer)
        runtime.releaseTransfer(transfer)
        runtime.stop()
        instrumentation.runOnMainSync { activity.finish() }
        return "PASS: G7 recreation/background retained fenced session and single writer"
    }

    /**
     * A fresh runtime instance models process replacement without attempting to retain a live
     * socket or writer. It must restore only the fence/recovery state and require fresh source
     * enumeration before camera traffic becomes eligible.
     */
    private fun processRestore(context: android.content.Context): String {
        val first = DurableSessionRuntime(PreferenceSessionStore(context))
        val lease = first.start()
        first.callback(lease.epoch, ConnectionEvent.TRANSPORT_READY)
        first.callback(lease.epoch, ConnectionEvent.LOST, ConnectionReason.NETWORK_LOSS)
        first.callback(lease.epoch, ConnectionEvent.RETRY_TIMER)
        val replacement = DurableSessionRuntime(PreferenceSessionStore(context))
        check(replacement.snapshot().epoch == lease.epoch)
        check(replacement.snapshot().recovery.state == ConnectionState.RECONNECTING)
        replacement.callback(lease.epoch, ConnectionEvent.TRANSPORT_READY)
        check(replacement.revalidated(lease.epoch, SourceObservation(false, false)) == SourceTrust.INCOMPLETE_UNTRUSTED)
        check(!CameraSessionCoordinator.mayUseCameraTraffic(replacement.snapshot()))
        check(replacement.revalidated(lease.epoch, SourceObservation(true, false)) == SourceTrust.TRUSTED)
        check(CameraSessionCoordinator.mayUseCameraTraffic(replacement.snapshot()))
        replacement.stop()
        val stopped = DurableSessionRuntime(PreferenceSessionStore(context))
        check(stopped.snapshot().userStopped)
        stopped.callback(lease.epoch, ConnectionEvent.TRANSPORT_READY)
        check(stopped.snapshot().userStopped)
        return "PASS: G7 process restoration retained fence, required revalidation and preserved user stop"
    }
}
