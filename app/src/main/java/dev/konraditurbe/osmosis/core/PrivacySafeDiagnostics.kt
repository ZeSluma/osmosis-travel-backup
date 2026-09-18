package dev.konraditurbe.osmosis.core

/** Last-line defence for user-shareable diagnostics. */
object PrivacySafeDiagnostics {
    private val mac = Regex("(?i)\\b(?:[0-9a-f]{2}:){5}[0-9a-f]{2}\\b")
    private val ipv4 = Regex("\\b(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)){3}\\b")
    private val uri = Regex("(?i)\\b(?:https?|rtsp)://[^\\s]+")
    private val credential = Regex("(?i)\\b(password|passphrase|pin)\\s*(?:=|:)\\s*(?:\\\"[^\\\"]*\\\"|\\S+)")
    private val ssid = Regex("(?i)\\bssid\\s*=\\s*\\\"[^\\\"]*\\\"")
    private val requestedNetwork = Regex("(?i)(wifi:\\s*requesting\\s*)\\\"[^\\\"]*\\\"")

    fun sanitize(message: String): String = message
        .replace(uri, "<redacted-uri>")
        .replace(mac, "<redacted-device>")
        .replace(ipv4, "<redacted-ip>")
        .replace(ssid, "SSID=<redacted>")
        .replace(requestedNetwork) { "${it.groupValues[1]}<redacted-network>" }
        .replace(credential) { "${it.groupValues[1]}=<redacted>" }
}
