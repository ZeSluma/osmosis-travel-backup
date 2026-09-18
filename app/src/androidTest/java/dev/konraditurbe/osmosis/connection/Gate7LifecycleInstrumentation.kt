package dev.konraditurbe.osmosis.connection

import android.app.Instrumentation
import android.content.Intent
import dev.konraditurbe.osmosis.ui.MainActivity

/** Emulator-only lifecycle proof; it never requests BLE, Wi-Fi, camera media, or a transfer. */
object Gate7LifecycleInstrumentation {
    fun run(instrumentation: Instrumentation, phase: String): String {
        check(phase == "recreate")
        check(android.os.Build.HARDWARE == "ranchu" && android.os.Build.MODEL.contains("sdk", true))
        val context = instrumentation.targetContext
        check(context.getSharedPreferences("osmosis", 0).all.isEmpty())
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
}
