package dev.konraditurbe.osmosis.core

import android.content.Context
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Process-wide "Save logs" file writer, shared by the UI and the background services.
 *
 * It deliberately does **not** live on the Activity: GPS sync runs as a foreground service and the
 * user is typically out with the phone (screen off, Activity destroyed, no adb in sight), so the
 * R-SDK/GPS lines have to keep landing in the file after the UI is gone. Every write is flushed, so
 * whatever happened is on disk even if the process is later killed.
 *
 * Files live in app-private storage. Verbose logging requires an explicit action in this process;
 * it expires after 30 minutes or 10 MiB and is never resumed from a stored preference.
 *
 * Never write coordinates or credentials here — this file is meant to be shared around.
 */
object FileLog {
    private const val KEEP = 5

    private val lock = Any()
    private var writer: Writer? = null
    private var file: File? = null
    private var startedAtMillis = 0L

    fun logsDir(ctx: Context): File = File(ctx.filesDir, "diagnostics/verbose").apply { mkdirs() }

    /** True while a log file is open. */
    fun isOn(): Boolean = synchronized(lock) { writer != null }

    /** The file currently (or most recently) being written, or null if none this process. */
    fun currentFile(): File? = synchronized(lock) { file }

    /** Opens a new explicit verbose session. No-op if one is already open. */
    fun start(ctx: Context) {
        synchronized(lock) {
            if (writer != null) return
            runCatching {
                val dir = logsDir(ctx)
                dir.listFiles { f -> f.isFile && f.name.endsWith(".log") }
                    ?.sortedByDescending { it.lastModified() }?.drop(KEEP - 1)?.forEach { it.delete() }
                val name = "osmosis_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.log"
                val f = File(dir, name)
                file = f
                writer = BufferedWriter(FileWriter(f, true))
                startedAtMillis = System.currentTimeMillis()
                writeLocked("VERBOSE_DIAGNOSTICS_STARTED")
            }.onFailure { android.util.Log.e("Osmosis", "verbose diagnostics unavailable") }
        }
    }

    fun stop() {
        synchronized(lock) {
            val w = writer ?: return
            writer = null
            startedAtMillis = 0L
            runCatching { w.flush(); w.close() }
        }
    }

    /** Append a timestamped line; no-op when logging is off. Safe from any thread. */
    fun write(s: String) = synchronized(lock) { writeLocked(PrivacySafeDiagnostics.sanitize(s)) }

    private fun writeLocked(s: String) {
        val w = writer ?: return
        val safe = PrivacySafeDiagnostics.sanitize(s)
        val bytes = safe.toByteArray(Charsets.UTF_8).size.toLong() + 32L
        if (!VerboseDiagnosticsPolicy.mayAppend(startedAtMillis, System.currentTimeMillis(), file?.length() ?: 0L, bytes)) {
            writer = null
            startedAtMillis = 0L
            runCatching { w.flush(); w.close() }
            android.util.Log.i("Osmosis", "verbose diagnostics limit reached")
            return
        }
        val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        runCatching { w.write("[$ts] $safe\n"); w.flush() }
    }
}
