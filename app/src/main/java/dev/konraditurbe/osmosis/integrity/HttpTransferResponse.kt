package dev.konraditurbe.osmosis.integrity

import java.net.HttpURLConnection

/** Connection is supplied by the explicitly bound network adapter; this class chooses no route/host. */
object HttpTransferResponse {
    fun open(connection:HttpURLConnection,offset:Long,evidence:(String)->Unit={}):TransferResponse {
        try {
            val parsed=ResponseHeaders.parse(connection.responseCode,connection.headerFields)
            evidence(TransportEvidence.summary(parsed.metadata,offset,parsed.etagPresent))
            return object:TransferResponse {
                override val metadata=parsed.metadata
                override fun input()=connection.inputStream
                override fun close(){connection.disconnect()}
            }
        }catch(error:Exception){connection.disconnect();throw error}
    }
}
