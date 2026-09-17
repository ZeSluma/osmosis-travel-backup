package dev.konraditurbe.osmosis.integrity

data class ParsedTransport(val metadata:ResponseMetadata,val etagPresent:Boolean)

/** Conservative fixed-length framing. Raw header values never leave this parser as diagnostics. */
object ResponseHeaders {
    fun parse(status:Int,headers:Map<out String?,List<String>>):ParsedTransport {
        fun single(name:String):String? {
            val values=headers.filterKeys{it?.equals(name,true)==true}.values.flatten()
            require(values.size<=1){"AMBIGUOUS_RESPONSE_HEADER"}
            return values.singleOrNull()?.also { require(it.none{c->c=='\r'||c=='\n'||c=='\u0000'}){"INVALID_RESPONSE_HEADER"} }?.trim(' ','\t')
        }
        // Even a simultaneously supplied Content-Length cannot disambiguate transfer coding here.
        require(single("Transfer-Encoding")==null){"UNSUPPORTED_TRANSFER_FRAMING"}
        val length=single("Content-Length")?.let {
            require(it.matches(Regex("[0-9]+"))){"INVALID_RESPONSE_LENGTH"}
            requireNotNull(it.toLongOrNull()){"INVALID_RESPONSE_LENGTH"}
        }
        val metadata=ResponseMetadata(status,length,single("Content-Range"),single("Content-Encoding"))
        return ParsedTransport(metadata,!single("ETag").isNullOrBlank())
    }
}
