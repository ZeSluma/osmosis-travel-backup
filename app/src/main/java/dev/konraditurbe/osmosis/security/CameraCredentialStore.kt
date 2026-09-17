package dev.konraditurbe.osmosis.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Authenticated private blobs; invoke off the UI thread. No credentials enter the ledger. */
class CameraCredentialStore(context: Context,
    private val legacy: SharedPreferences = context.getSharedPreferences("osmosis", 0),
    private val namespace: String = "camera-secrets-v1", private val checkpoint: (String) -> Unit = {}) {
    init { require(namespace.matches(Regex("[A-Za-z0-9-]+"))) }
    private val root = File(context.noBackupFilesDir, namespace)
    private fun id(camera: String) = MessageDigest.getInstance("SHA-256").digest(camera.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
    private fun blob(camera: String) = AtomicFile(File(root, id(camera)))
    private fun keyStore() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private fun alias(camera: String) = "$namespace-${id(camera)}"
    private fun secret(camera: String, create: Boolean): SecretKey {
        val stored = keyStore().getKey(alias(camera), null) as? SecretKey
        if (stored != null) return stored
        check(create) { "CREDENTIAL_UNAVAILABLE" }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias(camera), KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).setKeySize(256).build())
        }.generateKey()
    }
    private fun decrypt(camera: String): String {
        val bytes = blob(camera).readFully()
        require(bytes.size in 30..8192 && bytes[0] == 1.toByte())
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secret(camera, false), GCMParameterSpec(128, bytes.copyOfRange(1,13)))
        cipher.updateAAD(id(camera).toByteArray(Charsets.UTF_8))
        val plaintext = cipher.doFinal(bytes.copyOfRange(13,bytes.size))
        return try { plaintext.toString(Charsets.UTF_8) } finally { plaintext.fill(0) }
    }
    private fun syncDirectory(directory: File) {
        val fd=android.system.Os.open(directory.absolutePath, android.system.OsConstants.O_RDONLY,0)
        try { android.system.Os.fsync(fd) } finally { android.system.Os.close(fd) }
    }
    private fun persist(camera: String, value: String) {
        require(value.isNotEmpty() && value.toByteArray(Charsets.UTF_8).size <= 4096)
        check(root.isDirectory || root.mkdirs())
        syncDirectory(root.parentFile!!)
        val cipher=Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secret(camera,true))
        cipher.updateAAD(id(camera).toByteArray(Charsets.UTF_8))
        val plaintext=value.toByteArray(Charsets.UTF_8)
        val encrypted=try { cipher.doFinal(plaintext) } finally { plaintext.fill(0) }
        check(cipher.iv.size == 12)
        checkpoint("BEFORE_WRITE")
        val file=blob(camera)
        val stream=file.startWrite()
        try { stream.write(byteArrayOf(1)+cipher.iv+encrypted);stream.fd.sync();file.finishWrite(stream);syncDirectory(root) }
        catch (error: Exception) { file.failWrite(stream);throw error }
        checkpoint("AFTER_WRITE")
        check(decrypt(camera)==value) { "CREDENTIAL_WRITE_UNVERIFIED" }
        checkpoint("AFTER_VERIFY")
    }
    fun read(camera: String): String? = synchronized(lock) {
        try {
            val file=blob(camera)
            val old=legacy.getString("pass_$camera",null)
            val exists=file.baseFile.exists() || File(file.baseFile.path+".bak").exists()
            val value=if (exists) decrypt(camera) else old?.takeIf { it.isNotEmpty() }?.also { persist(camera,it) }
            if (value != null && old != null) {
                check(value==old) { "CREDENTIAL_MIGRATION_CONFLICT" }
                check(legacy.edit().remove("pass_$camera").commit())
                checkpoint("AFTER_RETIRE")
            }
            value
        } catch (_: Exception) { null }
    }
    fun save(camera: String, value: String): Boolean = synchronized(lock) {
        try { persist(camera,value);check(legacy.edit().remove("pass_$camera").commit());true }
        catch (_: Exception) { false }
    }
    /** Only the existing explicit Forget camera action may remove a camera credential. */
    fun forget(camera: String): Boolean = synchronized(lock) {
        try {
            check(legacy.edit().remove("pass_$camera").commit())
            blob(camera).delete()
            keyStore().deleteEntry(alias(camera))
            true
        } catch (_: Exception) { false }
    }
    companion object { private val lock=Any() }
}
