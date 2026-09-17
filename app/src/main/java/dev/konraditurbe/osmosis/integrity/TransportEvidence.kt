package dev.konraditurbe.osmosis.integrity

/** Diagnostic schema only: presence is never source-equivalence proof. No header values are emitted. */
object TransportEvidence {
    fun summary(response:ResponseMetadata,offset:Long,etagPresent:Boolean):String =
        "HTTP status=${response.status} length=${response.contentLength} offset=$offset " +
            "range_present=${response.contentRange!=null} identity_encoding=${response.contentEncoding==null || response.contentEncoding.equals("identity",true)} etag_present=$etagPresent"
}
