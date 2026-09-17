package dev.konraditurbe.osmosis.integrity

import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

/** Injected IO only. No file lookup, overwrite, delete, network or public-state authority. */
object CheckedCopy {
    enum class Outcome { COPIED, PARTIAL, REJECTED }
    data class Result(val outcome:Outcome,val received:Long,val durableBytes:Long,val digest:ByteArray?)
    /** Caller journals an exclusively owned staging destination before supplying output. */
    fun copy(expected:Long,offset:Long,response:ResponseMetadata,openInput:()->InputStream,
        openOwnedOutput:()->OutputStream, checkpoint:(Long)->Unit,
        cancelled:()->Boolean={false}):Result {
        val contract=RangeContract.validate(expected,offset,response) ?: return Result(Outcome.REJECTED,0,offset,null)
        var received=0L;var durable=offset
        val digest=MessageDigest.getInstance("SHA-256")
        return try {
            openInput().use { input ->
                openOwnedOutput().use { output ->
                    val buffer=ByteArray(65536)
                    while(true) {
                        if(cancelled()) return Result(Outcome.PARTIAL,received,durable,null)
                        val n=input.read(buffer)
                        if(n<0) break
                        if(n==0) throw java.io.IOException("NO_PROGRESS")
                        if(n.toLong()>contract.responseLength-received) throw java.io.IOException("EXCESS_BODY")
                        output.write(buffer,0,n);received+=n
                        digest.update(buffer,0,n)
                        output.flush()
                        // Callback must sync the destination then durably journal; flush alone is not durability.
                        checkpoint(offset+received);durable=offset+received
                    }
                    if(received!=contract.responseLength) throw java.io.IOException("SHORT_BODY")
                    output.flush()
                } // A close error cannot result in COPIED.
            }
            Result(Outcome.COPIED,received,durable,digest.digest())
        } catch(_:Exception) {Result(Outcome.PARTIAL,received,durable,null)}
    }
    /** Readback is separate from copy. Comparing a local digest never proves camera-source equality. */
    fun readBack(expected:Long,expectedDigest:ByteArray,open:()->InputStream):Boolean {
        if(expected<=0 || expectedDigest.size!=32)return false
        return try {
            var count=0L;val digest=MessageDigest.getInstance("SHA-256")
            open().use{input->
                val buffer=ByteArray(65536)
                while(true){val n=input.read(buffer);if(n<0)break
                    if(n==0 || n.toLong()>expected-count)return false
                    count+=n;digest.update(buffer,0,n)
                }
            }
            count==expected && MessageDigest.isEqual(expectedDigest,digest.digest())
        }catch(_:Exception){false}
    }
}
