package dev.konraditurbe.osmosis.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import dev.konraditurbe.osmosis.ledger.LedgerDatabase
import java.security.MessageDigest

/** Persists only a user-selected SAF tree grant; no raw external path or volume-name guesswork. */
class ExternalDestinationManager(private val context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("external_backup_destination", Context.MODE_PRIVATE)
    fun selectedTree(): Uri? = prefs.getString("tree", null)?.let(Uri::parse)
    fun configure(tree: Uri): String {
        require(DocumentsContract.isTreeUri(tree)) { "NOT_TREE_URI" }
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        app.contentResolver.takePersistableUriPermission(tree, flags)
        val id = "ssd-" + sha256(tree.toString()).take(24)
        prefs.edit().putString("tree", tree.toString()).putString("id", id).commit()
        val available = app.contentResolver.persistedUriPermissions.any { it.uri == tree && it.isReadPermission && it.isWritePermission }
        ReplicaEvidenceRepository(LedgerDatabase.open(app)).registerDestination(id, tree.toString(), available)
        return id
    }
    fun availability(): Boolean {
        return refreshAvailability()==ExternalStorageAvailability.AVAILABLE
    }
    /** Probe the user-approved SAF provider on every foreground/restart opportunity; grants alone do not prove an attached SSD. */
    fun refreshAvailability(): ExternalStorageAvailability {
        val tree=selectedTree() ?: return ExternalStorageAvailability.PERMISSION_REQUIRED
        val id=destinationId() ?: return ExternalStorageAvailability.PERMISSION_REQUIRED
        val grant=app.contentResolver.persistedUriPermissions.any { it.uri==tree && it.isReadPermission && it.isWritePermission }
        val bytes=SafDestinationProbe.availableBytes(app.contentResolver,tree)
        val state=ExternalStoragePolicy.assess(grant,bytes,1)
        runCatching { ReplicaEvidenceRepository(LedgerDatabase.open(app)).availability(id,state==ExternalStorageAvailability.AVAILABLE,state.name) }
        return state
    }
    fun destinationId(): String? = prefs.getString("id", null)
    private fun sha256(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }
}
