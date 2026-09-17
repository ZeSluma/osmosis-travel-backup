package dev.konraditurbe.osmosis.integrity

import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

/** Only an independently justified adapter may supply IMMUTABLE_VERSION; an ETag alone is UNKNOWN. */
enum class VersionTrust { UNKNOWN, IMMUTABLE_VERSION }
data class SourceRevision(val source:String,val asset:String,val version:String,val trust:VersionTrust)
data class PrefixProof(val binding:LocalBinding,val sha256:String,val source:SourceRevision)

interface ResumeDestination {
    fun inspect():LocalBinding
    fun input():InputStream
    /** Must recheck binding on the opened handle, seek to its exact end, and never truncate. */
    fun append(expected:LocalBinding):OutputStream
    fun sync()
}

/** Injected transfer kernel. No Pocket adapter currently supplies trusted source-version proof. */
object GuardedResume {
    enum class Outcome { COPIED_UNVERIFIED, REVIEW_REQUIRED, PARTIAL }
    data class Result(val outcome:Outcome,val durableBytes:Long,val confirmedBinding:LocalBinding?=null)
    fun copy(expected:Long,proof:PrefixProof,current:SourceRevision,response:ResponseMetadata,
        openTail:()->InputStream,destination:ResumeDestination,checkpoint:(Long)->Unit,
        cancelled:()->Boolean={false},durablePrefix:((Long,String)->Unit)?=null):Result {
        val prefix=proof.binding
        fun review()=Result(Outcome.REVIEW_REQUIRED,prefix.bytes)
        // All contract checks precede opening source body or append destination.
        if(prefix.bytes<=0 || prefix.bytes>=expected || !prefix.pending || !prefix.readable ||
            prefix.locator.isBlank() || prefix.revision.isBlank() ||
            !proof.sha256.matches(Regex("[0-9a-f]{64}")) ||
            proof.source.trust!=VersionTrust.IMMUTABLE_VERSION || current!=proof.source ||
            current.source.isBlank() || current.asset.isBlank() || current.version.isBlank() ||
            RangeContract.validate(expected,prefix.bytes,response)==null || cancelled()) return review()
        var appendAttempted=false
        var durable=prefix.bytes
        return try {
            if(destination.inspect()!=prefix)return review()
            val combined=readPrefix(prefix.bytes,destination::input) ?: return review()
            if(!MessageDigest.isEqual(decode(proof.sha256),(combined.clone() as MessageDigest).digest()) ||
                destination.inspect()!=prefix || cancelled())return review()
            val copied=CheckedCopy.copy(expected,prefix.bytes,response,openTail,
                {appendAttempted=true;destination.append(prefix)}, {n->
                    destination.sync();checkpoint(n);durable=n
                    durablePrefix?.invoke(n,(combined.clone() as MessageDigest).digest().joinToString(""){"%02x".format(it)})
                },cancelled,copiedBytes={bytes,offset,count->combined.update(bytes,offset,count)})
            if(copied.outcome!=CheckedCopy.Outcome.COPIED)return Result(Outcome.PARTIAL,copied.durableBytes)
            val after=destination.inspect()
            if(after.locator!=prefix.locator || !after.pending || !after.readable || after.bytes!=expected ||
                cancelled() || !readBackSegments(expected,prefix.bytes,decode(proof.sha256),checkNotNull(copied.digest),destination::input) ||
                destination.inspect()!=after)return Result(Outcome.PARTIAL,copied.durableBytes)
            // No source-equivalence repository write and no publication authority here.
            Result(Outcome.COPIED_UNVERIFIED,copied.durableBytes,after)
        }catch(_:Exception){if(appendAttempted)Result(Outcome.PARTIAL,durable) else review()}
    }

    private fun decode(hex:String)=ByteArray(32){hex.substring(it*2,it*2+2).toInt(16).toByte()}
    private fun readPrefix(expected:Long,open:()->InputStream):MessageDigest? {
        val digest=MessageDigest.getInstance("SHA-256");var count=0L
        open().use { input ->
            val buffer=ByteArray(65536)
            while(true){val n=input.read(buffer);if(n<0)break
                if(n==0 || n.toLong()>expected-count)return null
                count+=n;digest.update(buffer,0,n)
            }
        }
        return digest.takeIf{count==expected}
    }
    private fun readBackSegments(expected:Long,offset:Long,prefix:ByteArray,tail:ByteArray,open:()->InputStream):Boolean {
        val prefixDigest=MessageDigest.getInstance("SHA-256");val tailDigest=MessageDigest.getInstance("SHA-256")
        var count=0L
        open().use { input ->
            val buffer=ByteArray(65536)
            while(true) {
                val n=input.read(buffer);if(n<0)break
                if(n==0 || n.toLong()>expected-count)return false
                val prefixCount=(offset-count).coerceIn(0,n.toLong()).toInt()
                if(prefixCount>0)prefixDigest.update(buffer,0,prefixCount)
                if(prefixCount<n)tailDigest.update(buffer,prefixCount,n-prefixCount)
                count+=n
            }
        }
        return count==expected && MessageDigest.isEqual(prefix,prefixDigest.digest()) && MessageDigest.isEqual(tail,tailDigest.digest())
    }
}
