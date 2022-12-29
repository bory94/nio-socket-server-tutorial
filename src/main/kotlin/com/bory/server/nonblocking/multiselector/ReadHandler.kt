package com.bory.server.nonblocking.multiselector

import com.bory.server.log
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors

class ReadHandler(
    private val readerKey: String,
    private val socketChannelsManager: SocketChannelsManager,
    readSelectorManager: ReadSelectorsManager
) {
    @Volatile
    private var running: Boolean = true
    private val executor = Executors.newSingleThreadExecutor()
    private val readSelector = readSelectorManager.selector(readerKey)

    fun startup() {
        executor.submit {
            while (running) {
                handleRequestInternally()
            }
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
                        socketChannelsManager.closeSocketChannel(
                            readerKey,
                            socketChannel
                        )
                        return@forEach
                    }

                    key.isReadable -> {
                        val buffer =
                            socketChannelsManager.getByteBuffer(readerKey, socketChannel)
                        when (socketChannel.read(buffer)) {
                            -1 -> {
                                log("socketChannel closed ::: $socketChannel")
                                socketChannelsManager.closeSocketChannel(readerKey, socketChannel)
                            }

                            else -> {
                                buffer.flip()
                                val bytes = ByteArray(buffer.remaining())
                                buffer.get(bytes)
                                val message = String(bytes).trim()

                                val receivedMessage = "Received Message ::: $message"
                                log("\t$receivedMessage")

                                buffer.compact()
                                val sendMessageByteArray =
                                    "==> $receivedMessage\n> ".toByteArray()

                                socketChannel.write(ByteBuffer.wrap(sendMessageByteArray))
                                key.interestOps(SelectionKey.OP_READ)

                                if (message == "exit") {
                                    log("Closing Client::: $socketChannel")
                                    socketChannelsManager.closeSocketChannel(
                                        readerKey,
                                        socketChannel
                                    )
                                }
                            }
                        }
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                log("Socket Error Occurred")
                socketChannelsManager.closeSocketChannel(readerKey, socketChannel)

                e.printStackTrace()
            }
        }
        socketChannelsManager.removeClosedSocketChannel(readerKey)
    }

    fun shutdown() {
        running = false
        executor.shutdown()
    }

}