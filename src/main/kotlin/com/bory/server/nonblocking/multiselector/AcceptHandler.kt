package com.bory.server.nonblocking.multiselector

import com.bory.server.log
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.util.concurrent.Executors

class AcceptHandler(
    private val clientSocketChannelsManager: ClientSocketChannelsManager,
    private val readHandlersManager: ReadHandlersManager
) {
    val acceptSelector: Selector = Selector.open()

    @Volatile
    private var running: Boolean = true
    private val executor = Executors.newSingleThreadExecutor()

    fun startup() = executor.execute {
        while (running) {
            handleAcceptanceInternally()
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
                        accept(key)
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

    private fun accept(key: SelectionKey) {
        val serverSocketChannel = key.channel() as ServerSocketChannel
        val socketChannel = serverSocketChannel.accept().apply { configureBlocking(false) }
        log("Accept ::: $socketChannel")

        val readHandlerName = getRandomReadHandlerName()
        clientSocketChannelsManager.newSocketChannel(readHandlerName, socketChannel)

        val selector = readHandlersManager.selector(readHandlerName)
        socketChannel.register(selector, SelectionKey.OP_READ)

        val acceptMessage = "Client accepted::: ${socketChannel.remoteAddress}"
        val acceptMessageBytes = "$acceptMessage\n> ".toByteArray()
        socketChannel.write(ByteBuffer.wrap(acceptMessageBytes))

        log(acceptMessage)
    }

    private fun getRandomReadHandlerName(): String = readHandlersManager.keys().random()

    fun shutdown() {
        executor.shutdown()
        running = false
    }

}