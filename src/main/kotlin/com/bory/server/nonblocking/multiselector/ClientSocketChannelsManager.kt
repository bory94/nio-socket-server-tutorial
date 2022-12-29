package com.bory.server.nonblocking.multiselector

import com.bory.server.log
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap

private const val BYTE_BUFFER_SIZE = 1024

class ClientSocketChannelsManager {
    private val clientSocketChannelsByKey =
        ConcurrentHashMap<String, ConcurrentHashMap<SocketChannel, ByteBuffer>>()

    fun newSocketChannel(key: String, socketChannel: SocketChannel) {
        if (!clientSocketChannelsByKey.containsKey(key)) {
            clientSocketChannelsByKey[key] = ConcurrentHashMap()
        }
        clientSocketChannelsByKey[key]!![socketChannel] =
            ByteBuffer.allocateDirect(BYTE_BUFFER_SIZE)
    }

    fun byteBuffer(key: String, socketChannel: SocketChannel): ByteBuffer {
        if (!clientSocketChannelsByKey.containsKey(key)) {
            throw IllegalStateException("key[$key] not found in socketChannelsByKey")
        }

        if (!clientSocketChannelsByKey[key]!!.containsKey(socketChannel)) {
            log("SocketChannel[$socketChannel] not registered in socketChannelsByKey")
            newSocketChannel(key, socketChannel)
        }

        return clientSocketChannelsByKey[key]!![socketChannel]!!
    }

    fun removeAllClosedSocketChannel(key: String) {
        if (!clientSocketChannelsByKey.containsKey(key)) {
            return
        }

        clientSocketChannelsByKey[key]!!.keys.forEach { socketChannel ->
            if (!socketChannel.isOpen) {
                closeSocketChannel(key, socketChannel)
            }
        }
    }

    fun closeSocketChannel(key: String, socketChannel: SocketChannel) {
        if (!clientSocketChannelsByKey.containsKey(key)) {
            log("key[$key] not found in socketChannelsByKey")
            return
        }

        if (!clientSocketChannelsByKey[key]!!.containsKey(socketChannel)) {
            log("SocketChannel[$socketChannel] not registered in socketChannelsByKey")
            return
        }

        clientSocketChannelsByKey[key]!!.remove(socketChannel)
        socketChannel.close()
    }
}
