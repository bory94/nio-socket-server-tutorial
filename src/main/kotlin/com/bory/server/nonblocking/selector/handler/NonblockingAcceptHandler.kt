package com.bory.server.nonblocking.selector.handler

import com.bory.server.log
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService

class NonblockingAcceptHandler(
    private val socketChannels: ConcurrentHashMap<SocketChannel, ByteBuffer>,
    private val executor: ExecutorService
) : NonblockingHandler {
    override fun handle(key: SelectionKey) {
        val serverSocketChannel = key.channel() as ServerSocketChannel
        val socketChannel = serverSocketChannel.accept().apply { configureBlocking(false) }
        socketChannel.register(key.selector(), SelectionKey.OP_READ)

        executor.submit { handleInternal(socketChannel) }
    }

    private fun handleInternal(socketChannel: SocketChannel) {
        val buffer = ByteBuffer.allocateDirect(1024)
        socketChannels[socketChannel] = buffer

        val initialMessage = "Client accepted::: ${socketChannel.remoteAddress}"
        val initialMessageBytes =
            "$initialMessage\n> ".toByteArray()
        socketChannel.write(ByteBuffer.wrap(initialMessageBytes))

        log(initialMessage)
    }

}