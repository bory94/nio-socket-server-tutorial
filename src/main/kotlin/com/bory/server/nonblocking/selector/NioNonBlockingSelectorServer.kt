package com.bory.server.nonblocking.selector

import com.bory.server.log
import com.bory.server.nonblocking.NIO_NON_BLOCKING_SELECTOR_SERVER_PORT
import com.bory.server.nonblocking.selector.handler.NonblockingAcceptHandler
import com.bory.server.nonblocking.selector.handler.NonblockingReadHandler
import com.bory.server.nonblocking.selector.handler.NonblockingWriteHandler
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap

class NioNonBlockingSelectorServer(private val port: Int) {
    private val serverSocketChannel = ServerSocketChannel.open()
    private val selector = Selector.open()
    private val socketChannels = ConcurrentHashMap<SocketChannel, ByteBuffer>()

    private val acceptHandler = NonblockingAcceptHandler(socketChannels)
    private val readHandler = NonblockingReadHandler(socketChannels)
    private val writeHandler = NonblockingWriteHandler(socketChannels)

    init {
        serverSocketChannel.bind(InetSocketAddress(port))
        serverSocketChannel.configureBlocking(false)
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
    }

    fun startup() {
        log("Startup Server on Port[$port]...")
        try {
            while (true) {
                selector.selectNow()

                val selectionKeys = selector.selectedKeys()
                selectionKeys.forEach { key ->
                    handleSelectionKey(key)
                    selectionKeys.remove(key)
                }

                socketChannels.keys.removeIf { !it.isOpen }
            }
        } finally {
            acceptHandler.shutdown()
            log("Server on Port[$port] shut down")
        }
    }

    private fun handleSelectionKey(key: SelectionKey) {
        try {
            when {
                !key.isValid -> return
                key.isAcceptable -> acceptHandler.handle(key)
                key.isReadable -> readHandler.handle(key)
                key.isWritable -> writeHandler.handle(key)
                else -> {}
            }

        } catch (e: Exception) {
            log("Socket Error Occurred")
            val socketChannel = key.channel() as SocketChannel
            closeSocketChannel(socketChannel)

            e.printStackTrace()
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
    NioNonBlockingSelectorServer(NIO_NON_BLOCKING_SELECTOR_SERVER_PORT).startup()
}