package dev.konraditurbe.osmosis.security

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import dev.konraditurbe.osmosis.ui.MainActivity

/** Platform-only harness: run on an empty emulator, never on a user's paired-camera installation. */
class LauncherSecurityInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        start()
    }

    override fun onStart() {
        val results = Bundle()
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
            check(field("connecting") == false)
            check(field("shortcutConfirmation") == null)
            check(!dev.konraditurbe.osmosis.camera.CameraSession.debugNoWriteRefresh)
            check(!dev.konraditurbe.osmosis.camera.CameraSession.debugPageForce)
        }
    }
}
