package dev.konraditurbe.osmosis.ledger

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.content.ContentResolver
import android.system.Os
import android.system.OsConstants

/** Read-only metadata in the existing phone landing zones. Descriptor fstat only;
 * no payload reads or writes, file streams,
 * media hashes, permissions, broad storage crawl, log output or content writes.
 * Completeness is only for accessible MediaStore rows, not all device storage.
 */
class LocalMediaInventory(private val context: Context) {
    @Suppress("DEPRECATION")
    fun read(): LocalInventory {
        val rows = mutableListOf<LocalMediaObservation>()
        return try {
            val volume = MediaStore.VOLUME_EXTERNAL_PRIMARY
            val version: String? = MediaStore.getVersion(context, volume)
            if (version.isNullOrBlank()) return LocalInventory(emptyList(), false, LocalInventoryFailure.VERSION_UNAVAILABLE)
            val generation = if (Build.VERSION.SDK_INT >= 30) MediaStore.getGeneration(context, volume) else null
            val collection = MediaStore.Files.getContentUri(volume)
            val columns = mutableListOf("_id", "_display_name", "relative_path", "_size", "is_pending", "date_added", "date_modified")
            if (Build.VERSION.SDK_INT >= 30) columns += "generation_modified"
            val selection = LocalCandidatePolicy.roots.joinToString(" OR ") { "relative_path LIKE ?" }
            val args = LocalCandidatePolicy.roots.map { "$it%" }.toTypedArray()
            val queryUri = if (Build.VERSION.SDK_INT >= 30) collection else MediaStore.setIncludePending(collection)
            context.contentResolver.query(queryUri, columns.toTypedArray(), Bundle().apply {
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, args)
                if (Build.VERSION.SDK_INT >= 30) putInt(MediaStore.QUERY_ARG_MATCH_PENDING, MediaStore.MATCH_INCLUDE)
            }, null)?.use { c ->
                while (c.moveToNext()) {
                    if (rows.size >= 20_000) return LocalInventory(rows, false, LocalInventoryFailure.LIMIT_REACHED)
                    val name = c.getString(1) ?: continue
                    val dir = c.getString(2) ?: continue
                    if (!LocalCandidatePolicy.inLandingZone(dir)) continue
                    val uri = ContentUris.withAppendedId(collection, c.getLong(0))
                    // MediaStore size/generation may lag an external replacement.
                    // Stat the read-only descriptor without reading a single byte.
                    val stat = context.contentResolver.openFileDescriptor(uri, "r")?.use { Os.fstat(it.fileDescriptor) }
                        ?: return LocalInventory(rows, false, LocalInventoryFailure.FILE_UNAVAILABLE)
                    if (!OsConstants.S_ISREG(stat.st_mode)) return LocalInventory(rows, false, LocalInventoryFailure.NONREGULAR_FILE)
                    rows += LocalMediaObservation(uri.toString(), name, dir, stat.st_size, c.getInt(4) != 0,
                        key(version, c.getLong(5), c.getLong(6), if (Build.VERSION.SDK_INT >= 30) c.getLong(7) else null,
                            if (c.isNull(3)) null else c.getLong(3), stat.st_dev, stat.st_ino, stat.st_mtime, stat.st_ctime, stat.st_size))
                }
            } ?: return LocalInventory(emptyList(), false, LocalInventoryFailure.PROVIDER_UNAVAILABLE)
            val stable = version == MediaStore.getVersion(context, volume) &&
                (Build.VERSION.SDK_INT < 30 || generation == MediaStore.getGeneration(context, volume))
            LocalInventory(rows, stable, if (stable) null else LocalInventoryFailure.UNSTABLE_SNAPSHOT)
        } catch (_: SecurityException) {
            LocalInventory(rows, false, LocalInventoryFailure.ACCESS_DENIED)
        } catch (_: java.io.FileNotFoundException) {
            LocalInventory(rows, false, LocalInventoryFailure.FILE_UNAVAILABLE)
        } catch (_: Exception) {
            // Permission/provider/unmounted/unstable inventory never means no local copy.
            LocalInventory(rows, false, LocalInventoryFailure.PROVIDER_UNAVAILABLE)
        }
    }
}
