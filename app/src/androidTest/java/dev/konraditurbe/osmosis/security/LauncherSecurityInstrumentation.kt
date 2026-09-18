package dev.konraditurbe.osmosis.security

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import dev.konraditurbe.osmosis.ui.MainActivity

/** Synthetic modes require an empty emulator. Only explicit hardwareAudit=read-only
 * may run on a paired target at a controlled no-transfer restart checkpoint. */
class LauncherSecurityInstrumentation : Instrumentation() {
    private var ledgerPhase: String? = null
    private var lifecyclePhase: String? = null
    private var backupPhase: String? = null
    private var readOnlyHardwareAudit = false
    private var timestampEvidence = false
    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        ledgerPhase = arguments?.getString("ledgerPhase")
        lifecyclePhase = arguments?.getString("lifecyclePhase")
        backupPhase = arguments?.getString("backupPhase")
        readOnlyHardwareAudit = arguments?.getString("hardwareAudit") == "read-only"
        timestampEvidence = arguments?.getString("timestampEvidence") == "read-only"
        start()
    }

    override fun onStart() {
        val results = Bundle()
        if (readOnlyHardwareAudit) {
            // Dedicated early return: never reaches synthetic credential/ledger/intent tests.
            val projection = if (timestampEvidence) dev.konraditurbe.osmosis.ledger.Gate2ReadOnlyAudit.projectTimestamps()
                else dev.konraditurbe.osmosis.ledger.Gate2ReadOnlyAudit.project()
            results.putString("stream", projection)
            finish(if (projection.contains("BLOCKED_READ_ONLY_PROJECTION")) Activity.RESULT_CANCELED else Activity.RESULT_OK, results)
            return
        }
        ledgerPhase?.let { phase ->
            try {
                results.putString("stream", dev.konraditurbe.osmosis.ledger.LedgerInstrumentation.run(this, phase))
                finish(Activity.RESULT_OK, results)
            } catch (error: IllegalStateException) {
                results.putString("stream", error.message?.takeIf { it.startsWith("GATE2_ASSERTION_") } ?: "FAIL: synthetic ledger precondition")
                finish(Activity.RESULT_CANCELED, results)
            }
            return
        }
        lifecyclePhase?.let { phase ->
            try {
                results.putString("stream", dev.konraditurbe.osmosis.connection.Gate7LifecycleInstrumentation.run(this, phase))
                finish(Activity.RESULT_OK, results)
            } catch (_: Throwable) {
                results.putString("stream", "FAIL: synthetic GATE7 lifecycle assertion")
                finish(Activity.RESULT_CANCELED, results)
            }
            return
        }
        backupPhase?.let { phase ->
            try {
                check(phase=="replicaMigration")
                results.putString("stream", dev.konraditurbe.osmosis.backup.ReplicaInstrumentation.verify(this))
                finish(Activity.RESULT_OK, results)
            } catch (error: Throwable) {
                val marker=(error as? IllegalStateException)?.message?.takeIf { it.startsWith("REPLICA_MIGRATION_") }
                results.putString("stream", marker ?: "FAIL: synthetic replica migration assertion")
                finish(Activity.RESULT_CANCELED, results)
            }
            return
        }
        var activity: Activity? = null
        try {
            check(targetContext.getSharedPreferences("osmosis", 0).all.isEmpty()) {
                "Requires empty test installation"
            }
            val launched = startActivitySync(Intent(targetContext, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            activity = launched
            waitForIdleSync()
            assertInert(launched)
            val attacks = listOf(
                Intent.ACTION_MAIN, Intent.ACTION_VIEW, "untrusted.internal.COMMAND",
            )
            for (actionName in attacks) {
                targetContext.startActivity(Intent(targetContext, MainActivity::class.java).apply {
                    action = actionName
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("pin", "SYNTHETIC_TEST_ONLY")
                    putExtra("wifi", true)
                    putExtra("ssid", "SYNTHETIC_TEST_ONLY")
                    putExtra("pass", "SYNTHETIC_TEST_ONLY")
                    putExtra("offload", true)
                    putExtra("autoscan", true)
                    putExtra("nojoin", true)
                    putExtra("nowriterefresh", true)
                    putExtra("pageforce", true)
                    putExtra("pageauto", true)
                    putExtra("pagesize", Int.MAX_VALUE)
                    putExtra("shortcut_mac", 12345) // wrong type, not a real identifier
                })
                waitForIdleSync()
                assertInert(launched)
            }
            runOnMainSync { launched.finish() }
            waitForIdleSync()
            val cold = startActivitySync(Intent(targetContext, MainActivity::class.java).apply {
                action = "untrusted.internal.COMMAND"
                putExtra("pin", "SYNTHETIC_TEST_ONLY")
                putExtra("wifi", true)
                putExtra("offload", true)
                putExtra("shortcut_mac", ByteArray(10000))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            activity = cold
            waitForIdleSync()
            assertInert(cold)
            results.putString("stream", "PASS: normal launch, three hostile warm launches, malformed cold launch; no command authority\n")
            finish(Activity.RESULT_OK, results)
        } catch (_: Throwable) {
            // Do not echo arbitrary input, exceptions or app state into test output.
            results.putString("stream", "FAIL: launcher security assertion; inspect synthetic emulator only\n")
            finish(Activity.RESULT_CANCELED, results)
        } finally {
            activity?.let { runOnMainSync { it.finish() } }
        }
    }

    private fun assertInert(activity: Activity) {
        runOnMainSync {
            check(!activity.isFinishing)
            check(activity.intent.extras == null && activity.intent.data == null)
            fun field(name: String): Any? = MainActivity::class.java.getDeclaredField(name).apply {
                isAccessible = true
            }.get(activity)
            check(field("pairPin") == "osmo")
            check(field("autoPickMac") == null)
            check(!dev.konraditurbe.osmosis.connection.CameraConnectionService.resources(activity.applicationContext).connecting)
            check(field("shortcutConfirmation") == null)
            check(!dev.konraditurbe.osmosis.camera.CameraSession.debugNoWriteRefresh)
            check(!dev.konraditurbe.osmosis.camera.CameraSession.debugPageForce)
        }
    }
}
