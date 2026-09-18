package dev.konraditurbe.osmosis.backup

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract

enum class ExternalStorageAvailability { AVAILABLE, PERMISSION_REQUIRED, INSUFFICIENT_SPACE, UNAVAILABLE }

object ExternalStoragePolicy {
    fun assess(hasPersistedReadWriteGrant:Boolean, availableBytes:Long?, requiredBytes:Long):ExternalStorageAvailability = when {
        !hasPersistedReadWriteGrant -> ExternalStorageAvailability.PERMISSION_REQUIRED
        requiredBytes<=0L || availableBytes==null || availableBytes<0L -> ExternalStorageAvailability.UNAVAILABLE
        availableBytes<requiredBytes -> ExternalStorageAvailability.INSUFFICIENT_SPACE
        else -> ExternalStorageAvailability.AVAILABLE
    }
}

/** Provider metadata is a prerequisite, not a capacity estimate: unknown space fails closed. */
object SafDestinationProbe {
    fun availableBytes(resolver:ContentResolver,tree:Uri):Long? = runCatching {
        if(!DocumentsContract.isTreeUri(tree)) return null
        val treeDocumentId=DocumentsContract.getTreeDocumentId(tree)
        resolver.query(DocumentsContract.buildRootsUri(tree.authority!!),arrayOf(
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,DocumentsContract.Root.COLUMN_AVAILABLE_BYTES),null,null,null)?.use { cursor ->
            while(cursor.moveToNext()) if(cursor.getString(0)==treeDocumentId)
                return@use if(cursor.isNull(1)) null else cursor.getLong(1)
            null
        }
    }.getOrNull()
}
