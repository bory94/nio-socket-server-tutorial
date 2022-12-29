package com.bory.server.nonblocking.multiselector

import com.bory.server.log
import com.bory.server.nonblocking.readAndCreateSendMessage
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors

class ReadHandler(
    val readerKey: String,
    private val clientSocketChannelsManager: ClientSocketChannelsManager,
    private val readSelector: Selector
) {
    @Volatile
    private var running: Boolean = true
    private val executor = Executors.newSingleThreadExecutor()

    fun startup() = executor.execute {
        while (running) {
            handleRequestInternally()
        }
    }

    private fun handleRequestInternally() {
        readSelector.selectNow()
        val selectionKeys = readSelector.selectedKeys()

        selectionKeys.forEach { key ->
            selectionKeys.remove(key)
            val socketChannel = key.channel() as SocketChannel
            try {
                when {
                    !key.isValid -> {
                        clientSocketChannelsManager.closeSocketChannel(readerKey, socketChannel)
                    }

                    key.isReadable -> {
                        handleReadRequest(socketChannel, key)
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                log("Socket Error Occurred")
                clientSocketChannelsManager.closeSocketChannel(readerKey, socketChannel)

                e.printStackTrace()
            }
        }
        clientSocketChannelsManager.removeAllClosedSocketChannel(readerKey)
    }

    private fun handleReadRequest(
        socketChannel: SocketChannel,
        key: SelectionKey
    ) {
        val buffer =
            clientSocketChannelsManager.byteBuffer(readerKey, socketChannel)
        when (socketChannel.read(buffer)) {
            -1 -> {
                log("socketChannel closed ::: $socketChannel")
                clientSocketChannelsManager.closeSocketChannel(readerKey, socketChannel)
            }

            else -> {
                val (message, sendMessageByteArray) = readAndCreateSendMessage(buffer)

                socketChannel.write(ByteBuffer.wrap(sendMessageByteArray))
                key.interestOps(SelectionKey.OP_READ)

                if (message == "exit") {
                    log("Closing Client::: $socketChannel")
                    clientSocketChannelsManager.closeSocketChannel(readerKey, socketChannel)
                }
            }
        }
    }

    fun shutdown() {
        running = false
        executor.shutdown()
    }

}