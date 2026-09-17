package dev.konraditurbe.osmosis.integrity

import org.junit.Assert.*
import org.junit.Test
import java.io.*

class CheckedCopyTest {
    private val bytes=ByteArray(130000){(it%251).toByte()}
    private val response=ResponseMetadata(200,bytes.size.toLong(),null)
    @Test fun copyReadbackAndSourceRemainSeparate() {
        val output=ByteArrayOutputStream();val checkpoints=mutableListOf<Long>()
        val r=CheckedCopy.copy(bytes.size.toLong(),0,response,{bytes.inputStream()},{output},{checkpoints+=it})
        assertEquals(CheckedCopy.Outcome.COPIED,r.outcome);assertArrayEquals(bytes,output.toByteArray())
        assertEquals(bytes.size.toLong(),checkpoints.last())
        assertTrue(CheckedCopy.readBack(bytes.size.toLong(),r.digest!!){output.toByteArray().inputStream()})
        val changed=output.toByteArray().apply{this[0]=99}
        assertFalse(CheckedCopy.readBack(bytes.size.toLong(),r.digest){changed.inputStream()})
    }
    @Test fun ignoredRangeCannotEvenOpenOutput() {
        var opened=false
        val r=CheckedCopy.copy(bytes.size.toLong(),1,response,{error("must not read")},{opened=true;ByteArrayOutputStream()},{})
        assertEquals(CheckedCopy.Outcome.REJECTED,r.outcome);assertFalse(opened)
    }
    @Test fun shortAndExcessBodiesNeverCopySuccessfully() {
        for(body in listOf(bytes.copyOf(bytes.size-1),bytes.copyOf(bytes.size+1))) {
            val out=ByteArrayOutputStream()
            val r=CheckedCopy.copy(bytes.size.toLong(),0,response,{body.inputStream()},{out},{})
            assertEquals(CheckedCopy.Outcome.PARTIAL,r.outcome);assertNull(r.digest)
            assertTrue(out.size()<=bytes.size)
        }
    }
    @Test fun writeFlushCloseAndCheckpointFailuresRemainPartial() {
        for(fault in listOf("write","flush","close","checkpoint")) {
            val out=object:OutputStream(){override fun write(b:Int){if(fault=="write")throw IOException()}
                override fun flush(){if(fault=="flush")throw IOException()}
                override fun close(){if(fault=="close")throw IOException()}}
            val r=CheckedCopy.copy(bytes.size.toLong(),0,response,{bytes.inputStream()},{out},{if(fault=="checkpoint")throw IOException()})
            assertEquals(CheckedCopy.Outcome.PARTIAL,r.outcome);assertNull(r.digest)
            if(fault=="write" || fault=="flush" || fault=="checkpoint")assertEquals(0L,r.durableBytes)
        }
    }
    @Test fun cancellationAndDisconnectAreNotSuccess() {
        val cancelled=CheckedCopy.copy(bytes.size.toLong(),0,response,{bytes.inputStream()},{ByteArrayOutputStream()},{},{true})
        assertEquals(CheckedCopy.Outcome.PARTIAL,cancelled.outcome)
        val input=object:InputStream(){override fun read():Int{throw IOException()}}
        val failed=CheckedCopy.copy(bytes.size.toLong(),0,response,{input},{ByteArrayOutputStream()},{})
        assertEquals(CheckedCopy.Outcome.PARTIAL,failed.outcome)
    }
    @Test fun cancellationDuringBlockingReadDoesNotWriteUnjournaledBuffer() {
        var cancelled=false
        val input=object:InputStream(){
            override fun read(buffer:ByteArray,offset:Int,length:Int):Int {
                buffer[offset]=7;cancelled=true;return 1
            }
            override fun read():Int=error("bulk read required")
        }
        val output=ByteArrayOutputStream();val checkpoints=mutableListOf<Long>()
        val r=CheckedCopy.copy(1,0,ResponseMetadata(200,1,null),{input},{output},{checkpoints+=it},{cancelled})
        assertEquals(CheckedCopy.Outcome.PARTIAL,r.outcome)
        assertEquals(0,r.received);assertEquals(0,r.durableBytes)
        assertEquals(0,output.size());assertTrue(checkpoints.isEmpty())
    }
    @Test fun resumeDigestIsOnlySuffixAndCannotVerifyWholeFile() {
        val suffix=bytes.copyOfRange(65536,bytes.size);val output=ByteArrayOutputStream()
        val r=CheckedCopy.copy(bytes.size.toLong(),65536,ResponseMetadata(206,suffix.size.toLong(),"bytes 65536-129999/130000"),{suffix.inputStream()},{output},{})
        assertEquals(CheckedCopy.Outcome.COPIED,r.outcome)
        assertFalse(CheckedCopy.readBack(bytes.size.toLong(),r.digest!!){bytes.inputStream()})
    }
}
