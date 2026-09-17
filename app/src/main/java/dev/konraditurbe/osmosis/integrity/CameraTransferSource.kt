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
        return HttpTransferResponse.open(connection,offset,technicalEvidence)
    }
}
