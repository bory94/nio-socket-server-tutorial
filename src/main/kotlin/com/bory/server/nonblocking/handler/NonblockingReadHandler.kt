package com.bory.server.nonblocking.handler

import com.bory.server.log
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap

class NonblockingReadHandler(
    private val socketChannels: ConcurrentHashMap<SocketChannel, ByteBuffer>
) : NonblockingHandler {
    override fun handle(key: SelectionKey) {
        val socketChannel = key.channel() as SocketChannel
        if (!key.isValid) {
            closeSocketChannel(socketChannel)
            return
        }

        val buffer = socketChannels[socketChannel]
            ?: throw IllegalStateException("Buffer not found for $socketChannel")

        when (socketChannel.read(buffer)) {
            -1 -> {
                closeSocketChannel(socketChannel)
            }

            else -> handleRequest(key, socketChannel, buffer)
        }
    }

    private fun handleRequest(
        key: SelectionKey,
        socketChannel: SocketChannel,
        buffer: ByteBuffer,
    ) {
        buffer.flip()
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val message = String(bytes).trim()

        val receivedMessage = "Received Message ::: $message"
        log("\t$receivedMessage")

        buffer.compact()
        val sendMessageByteArray = "==> $receivedMessage\n> ".toByteArray()
        buffer.put(sendMessageByteArray, 0, sendMessageByteArray.size)

        key.interestOps(SelectionKey.OP_WRITE)

        if (message == "exit") {
            log("Closing Client::: $socketChannel")
            closeSocketChannel(socketChannel)
        }
    }

    private fun closeSocketChannel(socketChannel: SocketChannel) = try {
        log("Closing socketChannel ::: $socketChannel")

        socketChannels.remove(socketChannel)
        socketChannel.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}