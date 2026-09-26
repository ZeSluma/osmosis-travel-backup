package dev.konraditurbe.osmosis.integrity

import android.net.Network
import java.net.HttpURLConnection
import java.net.URL
import java.io.IOException

/** Explicit camera network only; diagnostics contain numeric/boolean fields, never paths or headers. */
class CameraTransferSource(
    private val network: Network?,
    private val technicalEvidence: (String) -> Unit = {},
    /** Test seam for the connection boundary; production always uses the bound camera network. */
    private val opener: ((String, Long) -> TransferResponse)? = null,
    private val sleeper: (Long) -> Unit = { Thread.sleep(it) },
) {
    fun open(path:String,offset:Long=0):TransferResponse {
        require(offset>=0 && path.startsWith("/v2?") && path.length<=8192 && path.none{it=='\r'||it=='\n'||it=='#'})
        opener?.let { return it(path, offset) }
        val connection=checkNotNull(network).openConnection(URL("http://192.168.2.1$path")) as HttpURLConnection
        connection.instanceFollowRedirects=false
        connection.connectTimeout=5000;connection.readTimeout=20000
        connection.requestMethod="GET"
        connection.setRequestProperty("Accept-Encoding","identity")
        connection.setRequestProperty("Connection","close")
        if(offset>0)connection.setRequestProperty("Range","bytes=$offset-")
        return HttpTransferResponse.open(connection,offset,technicalEvidence)
    }

    /**
     * Retry only the documented transient camera-busy responses. This occurs before the strict
     * transfer allocates a destination, so a refusal cannot create duplicate/partial media.
     */
    fun openForStrictTransfer(path: String, cancelled: () -> Boolean, onRetry: () -> Unit = {}): TransferResponse {
        var retries = 0
        while (true) {
            if (cancelled()) throw IOException("CANCELLED")
            val response = open(path)
            if (response.metadata.status in 200..299) return response
            val status = response.metadata.status
            response.close()
            if (!CameraResponseRetryPolicy.shouldRetry(status, retries)) {
                throw IOException("UNUSABLE_HTTP_STATUS")
            }
            onRetry()
            try {
                sleeper(CameraResponseRetryPolicy.delayMillis(retries))
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IOException("INTERRUPTED")
            }
            retries++
        }
    }
}
