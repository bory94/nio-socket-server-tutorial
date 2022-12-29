package com.bory.server.nonblocking.multiselector

import com.bory.server.log
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.util.concurrent.Executors

class AcceptHandler(
    private val socketChannelsManager: SocketChannelsManager,
    private val readSelectorManager: ReadSelectorsManager
) {
    val acceptSelector: Selector = Selector.open()

    @Volatile
    private var running: Boolean = true
    private val executor = Executors.newSingleThreadExecutor()

    fun startup() {
        executor.submit {
            while (running) {
                handleAcceptanceInternally()
            }
        }
    }

    private fun handleAcceptanceInternally() {
        acceptSelector.selectNow()
        val selectionKeys = acceptSelector.selectedKeys()
        selectionKeys.forEach { key ->
            try {
                when {
                    !key.isValid -> return@forEach
                    key.isAcceptable -> {
                        val serverSocketChannel = key.channel() as ServerSocketChannel
                        val socketChannel =
                            serverSocketChannel.accept().apply { configureBlocking(false) }

                        val readHandlerName = getRandomReadHandlerName()
                        socketChannelsManager.newSocketChannel(
                            readHandlerName,
                            socketChannel
                        )

                        val selector = readSelectorManager.selector(readHandlerName)
                        log("Accept ==> $selector")
                        socketChannel.register(
                            selector,
                            SelectionKey.OP_READ
                        )

                        val initialMessage =
                            "Client accepted::: ${socketChannel.remoteAddress}"
                        val initialMessageBytes =
                            "$initialMessage\n> ".toByteArray()
                        socketChannel.write(ByteBuffer.wrap(initialMessageBytes))

                        log(initialMessage)
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                log("Socket Error Occurred")
                e.printStackTrace()
            }

            selectionKeys.remove(key)

        }
    }

    private fun getRandomReadHandlerName(): String = readSelectorManager.keys().random()

    fun shutdown() {
        executor.shutdown()
        running = false
    }

}