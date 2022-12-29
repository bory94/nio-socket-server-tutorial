package com.bory.server.nonblocking

import com.bory.server.log
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

const val NIO_NON_BLOCKING_SERVER_PORT = 9996
const val NIO_NON_BLOCKING_POOL_SERVER_PORT = 9995
const val NIO_NON_BLOCKING_SELECTOR_SERVER_PORT = 9994
const val NIO_NON_BLOCKING_MULTIPLE_SELECTOR_SERVER_PORT = 9993

fun initializeAcceptedSocketChannel(socketChannel: SocketChannel) {
    log("Initializing Accepted SocketChannel ::: $socketChannel")
    socketChannel.configureBlocking(false)
    socketChannel.write(
        ByteBuffer.wrap("Client accepted::: ${socketChannel.remoteAddress}\n> ".toByteArray())
    )
}

fun processRequest(socketChannel: SocketChannel, buffer: ByteBuffer): String {
    buffer.flip()
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    val message = String(bytes).trim()
    val receivedMessage = "Received Message ::: $message"
    log("\t$receivedMessage")

    socketChannel.write(ByteBuffer.wrap("==> $receivedMessage\n> ".toByteArray()))
    buffer.compact()

    return message
}