package dev.konraditurbe.osmosis.backup

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.time.LocalDate

data class SafReplicaPath(val captureDay:String,val fileName:String) {
    companion object {
        fun parse(value:String):SafReplicaPath {
            require(value.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}/[A-Za-z0-9._-]{1,180}"))) { "UNSAFE_REPLICA_PATH" }
            val (day,name)=value.split('/',limit=2);LocalDate.parse(day)
            return SafReplicaPath(day,name)
        }
    }
}

/**
 * Storage Access Framework implementation for an approved external tree. It uses document URIs,
 * never raw mount paths, and creates a private `.part` document before an atomic-ish provider rename.
 * A provider that cannot support these operations remains unavailable; it is never treated as SSD-ready.
 */
class SafPendingReplica private constructor(
    private val resolver: ContentResolver,
    private val parent: Uri,
    private var documentUri: Uri,
    private val finalName: String
) : PendingReplica {
    override val locator: String get() = documentUri.toString()
    override fun output(): OutputStream = resolver.openOutputStream(documentUri, "w") ?: throw FileNotFoundException("OUTPUT_UNAVAILABLE")
    override fun input(): InputStream = resolver.openInputStream(documentUri) ?: throw FileNotFoundException("INPUT_UNAVAILABLE")
    override fun sync() { /* SAF exposes no portable fsync; close/read-back is mandatory verification. */ }
    override fun bytes(): Long = resolver.query(documentUri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use {
        check(it.moveToFirst()); it.getLong(0)
    } ?: throw FileNotFoundException("METADATA_UNAVAILABLE")
    override fun finalizeReplica() {
        // Check immediately before rename: a provider collision is review-required, never overwrite.
        check(!childNamed(resolver, parent, finalName)) { "FINAL_COLLISION_REVIEW_REQUIRED" }
        documentUri = checkNotNull(DocumentsContract.renameDocument(resolver, documentUri, finalName)) { "FINALIZE_UNSUPPORTED" }
    }

    companion object {
        fun create(resolver: ContentResolver, treeUri: Uri, relativePath: String, mime: String): SafPendingReplica {
            require(DocumentsContract.isTreeUri(treeUri)) { "NOT_TREE_URI" }
            require(mime.matches(Regex("[A-Za-z0-9.+-]+/[A-Za-z0-9.+-]+"))) { "UNSAFE_MIME" }
            val path=SafReplicaPath.parse(relativePath)
            val root = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
            val parent=child(resolver,root,path.captureDay)?.also { existing ->
                require(mimeOf(resolver,existing)==DocumentsContract.Document.MIME_TYPE_DIR) { "CAPTURE_DAY_COLLISION_REVIEW_REQUIRED" }
            } ?: checkNotNull(DocumentsContract.createDocument(resolver,root,DocumentsContract.Document.MIME_TYPE_DIR,path.captureDay)) { "CAPTURE_DAY_ALLOCATION_UNAVAILABLE" }
            check(!childNamed(resolver, parent, path.fileName)) { "FINAL_COLLISION_REVIEW_REQUIRED" }
            val temporary = ".osmosis-${UUID.randomUUID()}.part"
            val created = checkNotNull(DocumentsContract.createDocument(resolver, parent, mime, temporary)) { "ALLOCATION_UNAVAILABLE" }
            return SafPendingReplica(resolver, parent, created, path.fileName)
        }

        private fun mimeOf(resolver:ContentResolver,uri:Uri):String?=resolver.query(uri,arrayOf(DocumentsContract.Document.COLUMN_MIME_TYPE),null,null,null)?.use {
            if(it.moveToFirst())it.getString(0) else null
        }
        private fun child(resolver:ContentResolver,parent:Uri,name:String):Uri? {
            val children=DocumentsContract.buildChildDocumentsUriUsingTree(parent,DocumentsContract.getDocumentId(parent))
            resolver.query(children,arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME),null,null,null)?.use { cursor ->
                while(cursor.moveToNext())if(cursor.getString(1)==name)return DocumentsContract.buildDocumentUriUsingTree(parent,cursor.getString(0))
            } ?: throw FileNotFoundException("DIRECTORY_UNAVAILABLE")
            return null
        }

        private fun childNamed(resolver: ContentResolver, parent: Uri, name: String): Boolean {
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(parent, DocumentsContract.getDocumentId(parent))
            return resolver.query(children, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) if (cursor.getString(0) == name) return@use true
                false
            } ?: throw FileNotFoundException("DIRECTORY_UNAVAILABLE")
        }
    }
}
