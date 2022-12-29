package com.bory.server.nonblocking.selector.handler

import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap

class NonblockingWriteHandler(
    private val socketChannels: ConcurrentHashMap<SocketChannel, ByteBuffer>
) : NonblockingHandler {
    override fun handle(key: SelectionKey) {
        val socketChannel = key.channel() as SocketChannel
        val buffer = socketChannels[socketChannel]
            ?: throw IllegalStateException("Buffer not found for $socketChannel")

        buffer.flip()
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer)
        }
        key.interestOps(SelectionKey.OP_READ)
        buffer.compact()
    }
}