package dev.konraditurbe.osmosis.ui

/** External input may select a confirmation target, never authorize an operation. */
internal object LauncherInputPolicy {
    private val macPattern = Regex("[0-9A-Fa-f]{2}(:[0-9A-Fa-f]{2}){5}")

    /** A bare launcher intent is explicit user discovery intent, unlike a restored task or deep link. */
    fun freshLauncher(action: String?, hasData: Boolean, categories: Set<String>, hasExtras: Boolean): Boolean =
        action == "android.intent.action.MAIN" && !hasData && !hasExtras &&
            categories == setOf("android.intent.category.LAUNCHER")

    fun camera(
        action: String?,
        hasData: Boolean,
        categories: Set<String>,
        knownCameras: Set<String>,
        readMac: () -> String?,
    ): String? {
        if (action != "android.intent.action.VIEW" || hasData || categories.isNotEmpty()) return null
        // Malformed/unparcelable external bundles must not crash either Activity entry point.
        val mac = try { readMac() } catch (_: RuntimeException) { return null }
        if (mac == null || mac.length != 17 || !macPattern.matches(mac)) return null
        return knownCameras.firstOrNull { it.equals(mac, ignoreCase = true) }
    }
}
