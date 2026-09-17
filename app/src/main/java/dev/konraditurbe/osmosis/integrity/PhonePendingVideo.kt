package dev.konraditurbe.osmosis.integrity

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import java.io.*
import java.security.MessageDigest

/** Creates only a new pending video, never opens a discovered existing file for writing. */
class PhonePendingVideo(private val context:Context) {
    fun create(relativePath:String):OwnedPendingDestination {
        require(relativePath.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}/[A-Za-z0-9._-]+\\.(MP4|MOV|mp4|mov)")))
        val date=relativePath.substringBefore('/');java.time.LocalDate.parse(date)
        val name=relativePath.substringAfter('/');val folder="Movies/Osmosis/$date/"
        val resolver=context.contentResolver
        val collection=MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        @Suppress("DEPRECATION")
        val probe=MediaStore.setIncludePending(collection)
        resolver.query(probe,arrayOf("_id"),"relative_path=? AND _display_name=?",arrayOf(folder,name),null)?.use{
            check(!it.moveToFirst()) { "DESTINATION_COLLISION_REVIEW_REQUIRED" }
        } ?: error("DESTINATION_INVENTORY_UNAVAILABLE")
        val uri=checkNotNull(resolver.insert(collection,ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME,name);put(MediaStore.MediaColumns.RELATIVE_PATH,folder)
            put(MediaStore.MediaColumns.MIME_TYPE,if(name.endsWith("MOV",true))"video/quicktime" else "video/mp4")
            put(MediaStore.MediaColumns.IS_PENDING,1)
        }))
        // Provider-assigned names/paths must agree with the durable reservation; retain any orphan on failure.
        resolver.query(uri,arrayOf("_display_name","relative_path"),null,null,null)!!.use{
            check(it.moveToFirst() && it.getString(0)==name && it.getString(1)==folder)
        }
        // Insertion may create only a provider row; materialize this new owned file before inspection.
        checkNotNull(resolver.openFileDescriptor(uri,"rw")).use {
            check(it.statSize==0L) { "NONEMPTY_DESTINATION_PRESERVED" }
            it.fileDescriptor.sync()
        }
        return Destination(context,uri)
    }
    private class Destination(context:Context,private val uri:Uri):OwnedPendingDestination {
        private val resolver=context.contentResolver
        private var active:FileOutputStream?=null
        override val locator=uri.toString()
        override fun output():OutputStream {
            check(active==null)
            val fd=checkNotNull(resolver.openFileDescriptor(uri,"rw"))
            if(fd.statSize!=0L){fd.close();error("NONEMPTY_DESTINATION_PRESERVED")}
            val output=ParcelFileDescriptor.AutoCloseOutputStream(fd);active=output
            return object:FilterOutputStream(output){
                override fun write(b:ByteArray,off:Int,len:Int){out.write(b,off,len)}
                override fun close(){try{super.close()}finally{active=null}}
            }
        }
        override fun sync(){checkNotNull(active).fd.sync()}
        override fun input():InputStream=checkNotNull(resolver.openInputStream(uri))
        override fun inspect():LocalBinding {
            val pending=resolver.query(uri,arrayOf("is_pending"),null,null,null)!!.use{check(it.moveToFirst());it.getInt(0)!=0}
            // Full local digest, never a camera-equality claim. Only this newly allocated destination is read.
            val digest=MessageDigest.getInstance("SHA-256");var count=0L
            input().use{stream->val buf=ByteArray(65536);while(true){val n=stream.read(buf);if(n<0)break;check(n>0);count=Math.addExact(count,n.toLong());digest.update(buf,0,n)}}
            val revision=digest.digest().joinToString(""){"%02x".format(it)}
            return LocalBinding(locator,revision,count,true,pending)
        }
        override fun publish(){check(resolver.update(uri,ContentValues().apply{put(MediaStore.MediaColumns.IS_PENDING,0)},null,null)==1)}
    }
}
