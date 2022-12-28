package com.bory.server.nonblocking

import com.bory.server.log
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class NioBlockingServer(port: Int) {
    private val serverSocketChannel = ServerSocketChannel.open()

    init {
        serverSocketChannel.bind(InetSocketAddress(port))
    }

    fun startup() {
        log("Startup Server...")
        while (true) {
            val socketChannel = serverSocketChannel.accept()
            handleClient(socketChannel)
        }
    }

    private fun handleClient(socketChannel: SocketChannel) {
        val readBuffer = ByteBuffer.allocateDirect(2048)

        socketChannel.write(ByteBuffer.wrap("Client accepted::: ${socketChannel.remoteAddress}\n> ".toByteArray()))

        try {
            while (socketChannel.read(readBuffer) != -1) {
                val message = processRequest(socketChannel, readBuffer)

                if (message == "exit") {
                    socketChannel.close()
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun main() {
    NioBlockingServer(NIO_NON_BLOCKING_SERVER_PORT).startup()
}