package dev.konraditurbe.osmosis.ledger

import dev.konraditurbe.osmosis.core.CameraFile
import dev.konraditurbe.osmosis.core.MediaSession

data class EnumerationBatch(val files: List<CameraFile>, val pagesEnded: Boolean, val failed: Boolean)

/** Read-only protocol pagination. Bounded progress guards prevent a broken cursor from looping forever. */
object LedgerEnumerator {
    fun enumerate(session: MediaSession): EnumerationBatch = enumerate(
        { session.fetchFileList("192.168.2.1") }, { session.moreAvailable }, { session.fetchNextPage() })

    fun enumerate(first: () -> List<CameraFile>, more: () -> Boolean, next: () -> List<CameraFile>, maxPages: Int = 256): EnumerationBatch {
        require(maxPages > 0)
        val found = linkedMapOf<String, CameraFile>()
        try {
            var page = first()
            var pages = 0
            while (true) {
                val prior = found.size
                page.forEach { file ->
                    val locator = key(file.storage, file.path)
                    val old = found[locator]
                    check(old == null || old == file) { "UNSTABLE_ENUMERATION" }
                    found[locator] = file
                }
                pages++
                if (!more()) return EnumerationBatch(found.values.toList(), true, false)
                if (pages >= maxPages || (pages > 1 && found.size == prior)) return EnumerationBatch(found.values.toList(), false, true)
                page = next()
            }
        } catch (_: Exception) {
            return EnumerationBatch(found.values.toList(), false, true)
        }
    }
}
