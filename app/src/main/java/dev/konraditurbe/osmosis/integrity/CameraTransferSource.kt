package dev.konraditurbe.osmosis.integrity

import android.net.Network
import java.net.HttpURLConnection
import java.net.URL

/** Explicit camera network only; diagnostics contain numeric/boolean fields, never paths or headers. */
class CameraTransferSource(private val network:Network, private val technicalEvidence:(String)->Unit={}) {
    fun open(path:String,offset:Long=0):TransferResponse {
        require(offset>=0 && path.startsWith("/v2?") && path.length<=8192 && path.none{it=='\r'||it=='\n'||it=='#'})
        val connection=network.openConnection(URL("http://192.168.2.1$path")) as HttpURLConnection
        connection.instanceFollowRedirects=false
        connection.connectTimeout=5000;connection.readTimeout=20000
        connection.requestMethod="GET"
        connection.setRequestProperty("Accept-Encoding","identity")
        connection.setRequestProperty("Connection","close")
        if(offset>0)connection.setRequestProperty("Range","bytes=$offset-")
        try {
            fun single(name:String):String? {
                val values=connection.headerFields.filterKeys{it?.equals(name,true)==true}.values.flatten()
                require(values.size<=1) { "AMBIGUOUS_RESPONSE_HEADER" }
                return values.singleOrNull()
            }
            val metadata=ResponseMetadata(connection.responseCode,single("Content-Length")?.toLongOrNull(),single("Content-Range"),single("Content-Encoding"))
            technicalEvidence(TransportEvidence.summary(metadata,offset,!single("ETag").isNullOrBlank()))
            return object:TransferResponse {
                override val metadata=metadata
                override fun input()=connection.inputStream
                override fun close(){connection.disconnect()}
            }
        }catch(e:Exception){connection.disconnect();throw e}
    }
}
