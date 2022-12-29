package com.bory.server.nonblocking

import com.bory.server.log
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap

// Bad Practice. Don't do this.
class NioNonBlockingPoolingServer(private val port: Int) {
    private val serverSocketChannel = ServerSocketChannel.open()
    private val socketChannels = ConcurrentHashMap<SocketChannel, ByteBuffer>()

    init {
        serverSocketChannel.bind(InetSocketAddress(port))
        // configure server socket as non-blocking
        serverSocketChannel.configureBlocking(false)
    }

    fun startup() {
        log("Startup Server on Port[$port]...")
        while (true) {
            val socketChannel = serverSocketChannel.accept()
            if (socketChannel != null && !socketChannels.containsKey(socketChannel)) {
                initializeAcceptedSocketChannel(socketChannel)
                socketChannels[socketChannel] = ByteBuffer.allocateDirect(1024)
            }

            socketChannels.forEach(::handleRequest)
            socketChannels.keys.removeIf { !it.isOpen }
        }
    }

    private fun handleRequest(socketChannel: SocketChannel, buffer: ByteBuffer) {
        try {
            when (val read = socketChannel.read(buffer)) {
                -1 -> {
                    log("Closing socketChannel ::: $socketChannel ::: read = $read")
                    closeSocketChannel(socketChannel)
                }

                0 -> {}

                else -> {
                    val message = processRequest(socketChannel, buffer)

                    if (message == "exit") {
                        closeSocketChannel(socketChannel)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            closeSocketChannel(socketChannel)
        }
    }

    private fun closeSocketChannel(socketChannel: SocketChannel) = try {
        socketChannels.remove(socketChannel)
        socketChannel.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun main() {
    NioNonBlockingPoolingServer(NIO_NON_BLOCKING_POOL_SERVER_PORT).startup()
}