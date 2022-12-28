package com.bory.server.nonblocking

import com.bory.server.log
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap

class NioNonBlockingSelectorServer(port: Int) {
    private val serverSocketChannel = ServerSocketChannel.open()
    private val selector = Selector.open()
    private val socketChannels = ConcurrentHashMap<SocketChannel, ByteBuffer>()

    init {
        serverSocketChannel.bind(InetSocketAddress(port))
        serverSocketChannel.configureBlocking(false)
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
    }

    fun startup() {
        log("Startup Server...")
        while (true) {
            selector.select()

            val selectionKeys = selector.selectedKeys()
            selectionKeys.iterator().forEach { key ->
                selectionKeys.remove(key)
                handleSelectionKey(key)
            }

            socketChannels.keys.removeIf { !it.isOpen }
        }
    }

    private fun handleSelectionKey(key: SelectionKey) {
        try {
            if (!key.isValid) {
                return
            }

            when {
                key.isAcceptable -> accept(key)
                key.isReadable -> read(key)
                key.isWritable -> write(key)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun accept(key: SelectionKey) {
        val serverSocketChannel = key.channel() as ServerSocketChannel
        val socketChannel = serverSocketChannel.accept().apply { configureBlocking(false) }
        socketChannel.register(key.selector(), SelectionKey.OP_READ)

        val buffer = ByteBuffer.allocateDirect(1024)
        socketChannels[socketChannel] = buffer

        val initialMessage = "Client accepted::: ${socketChannel.remoteAddress}"
        val initialMessageBytes =
            "$initialMessage\n> ".toByteArray()
        socketChannel.write(ByteBuffer.wrap(initialMessageBytes))

        log(initialMessage)
    }

    private fun read(key: SelectionKey) {
        val socketChannel = key.channel() as SocketChannel
        val buffer = socketChannels[socketChannel]
            ?: throw IllegalStateException("Buffer not found for $socketChannel")

        buffer.info("READ")
        when (val read = socketChannel.read(buffer)) {
            -1 -> {
                log("Closing socketChannel ::: $socketChannel ::: read = $read")
                closeSocketChannel(socketChannel)
            }

            else -> {
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
                    closeSocketChannel(socketChannel)
                }
            }
        }

    }

    private fun write(key: SelectionKey) {
        val socketChannel = key.channel() as SocketChannel
        val buffer = socketChannels[socketChannel]
            ?: throw IllegalStateException("Buffer not found for $socketChannel")

        buffer.flip()
        buffer.info("WRITE")
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer)
        }
        key.interestOps(SelectionKey.OP_READ)
        buffer.compact()
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