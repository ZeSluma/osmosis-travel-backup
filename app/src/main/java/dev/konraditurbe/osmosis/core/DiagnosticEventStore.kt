package dev.konraditurbe.osmosis.core

import android.content.Context
import java.io.File
import java.time.Duration

/**
 * Small, app-private diagnostic channel.  It intentionally accepts only allowlisted codes and
 * numbers: callers cannot turn a filename, location, credential, packet or exception message into
 * an exportable diagnostic field.  Ledger and transfer truth remain in their own durable stores.
 */
class DiagnosticEventStore private constructor(
    private val directory: File,
    private val nowMillis: () -> Long,
    private val maxBytes: Long,
    private val maxAgeMillis: Long,
) {
    enum class Type {
        SESSION_STARTED,
        SESSION_STATE,
        CAMERA_SESSION_READY,
        RECONNECT_SCHEDULED,
        RECONNECT_ATTEMPT,
        RECONNECT_FAILED,
        TRANSFER_STATE,
        STORAGE_STATE,
        USER_ACTION_REQUIRED,
    }

    @Synchronized fun record(
        type: Type,
        oldState: String? = null,
        newState: String? = null,
        reason: String? = null,
        retryCount: Int? = null,
        bytes: Long? = null,
    ) {
        // Diagnostics are never a dependency of the backup state machine.
        runCatching {
            require(retryCount == null || retryCount in 0..1_000_000)
            require(bytes == null || bytes in 0..Long.MAX_VALUE)
            directory.mkdirs()
            prune()
            val line = listOf(
                nowMillis().toString(), type.name, code(oldState), code(newState), code(reason),
                retryCount?.toString() ?: "", bytes?.toString() ?: ""
            ).joinToString("|") + "\n"
            File(directory, "events-${nowMillis() / DAY_MILLIS}.log").appendText(line, Charsets.UTF_8)
            prune()
        }
    }

    /** Copies only this already allowlisted representation for an explicit user export action. */
    @Synchronized fun exportTo(destination: File): File? = runCatching {
        directory.mkdirs()
        prune()
        val text = files().joinToString(separator = "") { it.readText(Charsets.UTF_8) }
        destination.parentFile?.mkdirs()
        destination.writeText(text, Charsets.UTF_8)
        destination
    }.getOrNull()

    private fun code(value: String?): String = when {
        value == null -> ""
        value.matches(Regex("[A-Z0-9_]{1,80}")) -> value
        else -> "REDACTED"
    }

    private fun files(): List<File> = directory.listFiles { file -> file.isFile && file.name.matches(Regex("events-[0-9]+\\.log")) }
        ?.sortedBy { it.lastModified() } ?: emptyList()

    private fun prune() {
        val cutoff = nowMillis() - maxAgeMillis
        files().filter { it.lastModified() < cutoff }.forEach { it.delete() }
        var total = files().sumOf { it.length() }
        for (file in files()) {
            if (total <= maxBytes) break
            val size = file.length()
            if (file.delete()) total -= size
        }
    }

    companion object {
        private const val DAY_MILLIS = 86_400_000L
        private const val DEFAULT_MAX_BYTES = 10L * 1024L * 1024L
        private val DEFAULT_MAX_AGE = Duration.ofDays(7).toMillis()

        fun open(context: Context): DiagnosticEventStore = DiagnosticEventStore(
            File(context.applicationContext.filesDir, "diagnostics"), System::currentTimeMillis,
            DEFAULT_MAX_BYTES, DEFAULT_MAX_AGE,
        )

        internal fun forTest(directory: File, nowMillis: () -> Long, maxBytes: Long, maxAgeMillis: Long) =
            DiagnosticEventStore(directory, nowMillis, maxBytes, maxAgeMillis)
    }
}
