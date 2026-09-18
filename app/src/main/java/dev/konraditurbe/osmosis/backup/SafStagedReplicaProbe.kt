package dev.konraditurbe.osmosis.backup

import android.content.ContentResolver
import android.net.Uri
import java.io.FileNotFoundException
import java.security.MessageDigest

/** Reads only the already-owned staged locator; unknown provider failures are never treated as missing. */
object SafStagedReplicaProbe {
    fun observe(resolver:ContentResolver, locator:String, expected:ReplicaProof):StagedReplicaObservation = try {
        var bytes=0L;val digest=MessageDigest.getInstance("SHA-256")
        val input=resolver.openInputStream(Uri.parse(locator)) ?: return StagedReplicaObservation.UNAVAILABLE
        input.use { stream ->
            val buffer=ByteArray(64*1024)
            while(true) { val read=stream.read(buffer);if(read<0)break;if(read==0)return StagedReplicaObservation.UNAVAILABLE
                bytes+=read; if(bytes>expected.bytes)return StagedReplicaObservation.CORRUPT;digest.update(buffer,0,read) }
        }
        ExternalReplicaReconciliation.classify(expected,bytes,ReplicaVerification.hex(digest.digest()))
    } catch(_:FileNotFoundException) { StagedReplicaObservation.MISSING }
    catch(_:SecurityException) { StagedReplicaObservation.UNAVAILABLE }
    catch(_:Exception) { StagedReplicaObservation.UNAVAILABLE }
}
