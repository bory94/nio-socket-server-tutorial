package com.bory.server.nonblocking.multiselector

import com.bory.server.log
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap

private const val BYTE_BUFFER_SIZE = 1024

class SocketChannelsManager {
    private val socketChannelsByKey =
        ConcurrentHashMap<String, ConcurrentHashMap<SocketChannel, ByteBuffer>>()

    fun newSocketChannel(key: String, socketChannel: SocketChannel) {
        if (!socketChannelsByKey.containsKey(key)) {
            socketChannelsByKey[key] = ConcurrentHashMap()
        }
        socketChannelsByKey[key]!![socketChannel] = ByteBuffer.allocateDirect(BYTE_BUFFER_SIZE)
    }

    fun getByteBuffer(key: String, socketChannel: SocketChannel): ByteBuffer {
        if (!socketChannelsByKey.containsKey(key)) {
            throw IllegalStateException("key[$key] not found in socketChannelsByKey")
        }

        if (!socketChannelsByKey[key]!!.containsKey(socketChannel)) {
            log("SocketChannel[$socketChannel] not registered in socketChannelsByKey")
            newSocketChannel(key, socketChannel)
        }

        return socketChannelsByKey[key]!![socketChannel]!!
    }

    fun removeClosedSocketChannel(key: String) {
        if (!socketChannelsByKey.containsKey(key)) {
            return
        }

        socketChannelsByKey[key]!!.keys.forEach { socketChannel ->
            if (!socketChannel.isOpen) {
                closeSocketChannel(key, socketChannel)
            }
        }
    }

    fun closeSocketChannel(key: String, socketChannel: SocketChannel) {
        if (!socketChannelsByKey.containsKey(key)) {
            throw IllegalStateException("key[$key] not found in socketChannelsByKey")
        }

        if (!socketChannelsByKey[key]!!.containsKey(socketChannel)) {
            log("SocketChannel[$socketChannel] not registered in socketChannelsByKey")
            return
        }

        socketChannelsByKey[key]!!.remove(socketChannel)
        socketChannel.close()
    }
}
