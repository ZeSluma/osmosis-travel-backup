package dev.konraditurbe.osmosis.ledger

import android.content.Context
import dev.konraditurbe.osmosis.security.CameraCredentialStore
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest

object CredentialInstrumentation {
    fun verify(context: Context) {
        val canary="SYNTHETIC-CREDENTIAL-NOT-REAL"
        for (boundary in listOf("BEFORE_WRITE","AFTER_WRITE","AFTER_VERIFY","AFTER_RETIRE")) {
            val ns="gate2-credential-${System.nanoTime()}"
            val prefs=context.getSharedPreferences(ns,0)
            check(prefs.edit().putString("pass_camera-a",canary).commit())
            val failing=CameraCredentialStore(context,prefs,ns) { stage -> if (stage==boundary) error("SYNTHETIC_CRASH") }
            check(failing.read("camera-a")==null)
            if (boundary!="AFTER_RETIRE") check(prefs.contains("pass_camera-a"))
            val restored=CameraCredentialStore(context,prefs,ns)
            check(restored.read("camera-a")==canary)
            check(!prefs.contains("pass_camera-a"))
            check(restored.read("camera-b")==null)
            File(context.noBackupFilesDir,ns).listFiles()!!.forEach { check(!it.readBytes().toString(Charsets.ISO_8859_1).contains(canary)) }
        }
        val ns="gate2-credential-${System.nanoTime()}"
        val prefs=context.getSharedPreferences(ns,0)
        val store=CameraCredentialStore(context,prefs,ns)
        fun id(camera: String)=MessageDigest.getInstance("SHA-256").digest(camera.toByteArray()).joinToString("") { "%02x".format(it) }
        fun file(camera: String)=File(File(context.noBackupFilesDir,ns),id(camera))
        check(store.save("camera-a",canary));val first=file("camera-a").readBytes()
        check(store.save("camera-a",canary));check(!first.contentEquals(file("camera-a").readBytes()))
        check(store.save("camera-b","SYNTHETIC-SECOND"))
        val second=file("camera-b").readBytes()
        file("camera-b").writeBytes(first)
        check(store.read("camera-b")==null) // authenticated camera context, no cross-camera disclosure
        file("camera-b").writeBytes(second)
        check(store.read("camera-b")=="SYNTHETIC-SECOND")
        val damaged=file("camera-a").readBytes();damaged[damaged.lastIndex]=(damaged.last().toInt() xor 1).toByte()
        file("camera-a").writeBytes(damaged);check(store.read("camera-a")==null)
        val keys=KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        keys.deleteEntry("$ns-${id("camera-a")}") // synthetic key only, models restore/key loss
        check(store.read("camera-a")==null)
        check(store.save("camera-a",canary)) // explicit re-entry recovers missing key
        check(store.read("camera-a")==canary)
        check(store.forget("camera-a"));check(store.read("camera-a")==null)
        check(store.read("camera-b")=="SYNTHETIC-SECOND")
    }
}
