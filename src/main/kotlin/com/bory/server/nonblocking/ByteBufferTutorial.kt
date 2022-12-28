package com.bory.server.nonblocking

import com.bory.server.log
import java.nio.ByteBuffer

fun main() {
    // string to ByteBuffer
    val buffer = ByteBuffer.wrap("Hello World1".toByteArray())
    buffer.info("After ByteBuffer.wrap")

    // read buffer
    val bytes = buffer.array()
    buffer.info("After ByteBuffer.array()")
    log("String(bytes) = " + String(bytes))

    val bytes2 = buffer.array()
    buffer.info("After ByteBuffer.array() again")
    log("String(bytes2) = " + String(bytes2))

    log("========================================================================")

    // allocate first, put next
    val buffer2 = ByteBuffer.allocate(80)
    buffer2.info("After ByteBuffer.allocate(80)")
    "Hello World2".toByteArray().forEach(buffer2::put)
    buffer2.info("After ByteBuffer.put(Byte) * n")
    buffer2.flip()
    buffer2.info("After ByteBuffer.flip()")

    val bytes3 = ByteArray(buffer2.remaining())
    buffer2.get(bytes3)
    buffer2.info("After Buffer.get(ByteArray)")
    log("String(bytes3) = " + String(bytes3))

    buffer2.flip()
    val bytes4 = buffer2.array()
    buffer2.info("After Buffer.array()")
    log("String(bytes4, 0, buffer2.remaining()) = " + String(bytes4, 0, buffer2.remaining()))

    // what is compact?
    buffer2.compact()
    buffer2.info("After ByteBuffer.compact")

    log("========================================================================")

    val buffer3 = ByteBuffer.allocate(80)
    buffer3.info("After ByteBuffer.allocate(80) again")
    "Hello World3".toByteArray().forEach(buffer3::put)
    buffer3.flip()

    val bytes5 = ByteArray(buffer3.remaining())
    buffer3.get(bytes5)
    buffer3.info("First getting from buffer3")
    log("String(bytes5) = ${String(bytes5)}")

    // put more after compact
    buffer3.compact()
    buffer3.info("buffer3 after compact")
    "Hello World4".toByteArray().forEach(buffer3::put)
    buffer3.info("Put data again - Hello World4")
    buffer3.flip()
    buffer3.info("flip again")

    val bytes6 = ByteArray(buffer3.remaining())
    buffer3.get(bytes6)
    log("String(bytes6) = ${String(bytes6)}")
}

fun ByteBuffer.info(message: String? = null) {
    if (message != null) {
        log("------------------ $message ------------------ ")
    }
    log("mark: ${mark()}")
    log("remaining: ${remaining()}")

    log("-------------------------------------------------------------")
}
