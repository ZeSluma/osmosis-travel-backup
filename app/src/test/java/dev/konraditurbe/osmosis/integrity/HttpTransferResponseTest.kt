package dev.konraditurbe.osmosis.integrity

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.net.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Loopback HTTP bytes, no external network or camera. Exercise actual JDK HTTP parsing and body IO. */
class HttpTransferResponseTest {
    private fun serve(response:String,test:(HttpURLConnection)->Unit) {
        ServerSocket(0,1,InetAddress.getLoopbackAddress()).use { server ->
            server.soTimeout=5000
            val executor=Executors.newSingleThreadExecutor()
            val worker=executor.submit {
                server.accept().use{socket->
                    socket.soTimeout=5000
                    val reader=socket.getInputStream().bufferedReader(Charsets.US_ASCII)
                    while(!reader.readLine().isNullOrEmpty()){}
                    socket.getOutputStream().write(response.toByteArray(Charsets.US_ASCII));socket.getOutputStream().flush()
                }
            }
            try {
                val connection=URL("http://127.0.0.1:${server.localPort}/synthetic").openConnection() as HttpURLConnection
                connection.instanceFollowRedirects=false;connection.connectTimeout=5000;connection.readTimeout=5000
                connection.setRequestProperty("Connection","close")
                test(connection);worker.get(6,TimeUnit.SECONDS)
            }finally{executor.shutdownNow()}
        }
    }
    @Test fun real206BodyPassesExactTailCopy(){
        serve("HTTP/1.1 206 Partial Content\r\nContent-Length: 3\r\nContent-Range: bytes 3-5/6\r\nConnection: close\r\n\r\ndef"){connection->
            HttpTransferResponse.open(connection,3).use { response ->
                val output=ByteArrayOutputStream()
                val copied=CheckedCopy.copy(6,3,response.metadata,response::input,{output},{})
                assertEquals(CheckedCopy.Outcome.COPIED,copied.outcome);assertEquals("def",output.toString("US-ASCII"))
                assertNull(response.sourceRevision)
            }
        }
    }
    @Test fun ignoredRangeAnd416NeverOpenOutput(){
        for(raw in listOf("200 OK\r\nContent-Length: 6","416 Range Not Satisfiable\r\nContent-Length: 0\r\nContent-Range: bytes */6"))
            serve("HTTP/1.1 $raw\r\nConnection: close\r\n\r\nabcdef"){connection->
                HttpTransferResponse.open(connection,3).use { response ->
                    val copied=CheckedCopy.copy(6,3,response.metadata,{error("body opened")},{error("output opened")},{})
                    assertEquals(CheckedCopy.Outcome.REJECTED,copied.outcome)
                }
            }
    }
    @Test fun duplicateLengthsAndTransferCodingFailClosed(){
        for(headers in listOf("Content-Length: 3\r\nContent-Length: 6","Content-Length: 3\r\nTransfer-Encoding: chunked"))
            serve("HTTP/1.1 200 OK\r\n$headers\r\nConnection: close\r\n\r\nabc"){connection->
                assertTrue(runCatching{HttpTransferResponse.open(connection,0)}.isFailure)
            }
    }
    @Test fun prematureSocketCloseCannotConfirmTransfer(){
        serve("HTTP/1.1 200 OK\r\nContent-Length: 6\r\nConnection: close\r\n\r\nabc"){connection->
            HttpTransferResponse.open(connection,0).use{response->
                val output=ByteArrayOutputStream()
                assertEquals(CheckedCopy.Outcome.PARTIAL,CheckedCopy.copy(6,0,response.metadata,response::input,{output},{}).outcome)
                assertEquals("abc",output.toString("US-ASCII"))
            }
        }
    }
    @Test fun redirectIsNotFollowedAndOpaqueEtagDoesNotBecomeProof(){
        val logs=mutableListOf<String>()
        serve("HTTP/1.1 302 Found\r\nLocation: http://invalid.invalid/\r\nContent-Length: 0\r\nETag: secret-fixture-marker\r\nConnection: close\r\n\r\n"){connection->
            HttpTransferResponse.open(connection,0,logs::add).use{response->
                assertEquals(302,response.metadata.status);assertNull(response.sourceRevision)
                assertNull(RangeContract.validate(6,0,response.metadata))
            }
        }
        assertTrue(logs.single().contains("etag_present=true"));assertFalse(logs.joinToString().contains("secret-fixture-marker"))
    }
}
