package com.bory.server.nonblocking.multiselector

import com.bory.server.log
import com.bory.server.nonblocking.NIO_NON_BLOCKING_MULTIPLE_SELECTOR_SERVER_PORT
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel

private const val READER_SIZE = 5

class NioNonBlockingMultipleSelectorServer(private val port: Int) {
    @Volatile
    private var running: Boolean = true
    private val serverSocketChannel = ServerSocketChannel.open()
    private val socketChannelsManager = SocketChannelsManager()

    private val readSelectorManager = ReadSelectorsManager()
    private val acceptHandler = AcceptHandler(socketChannelsManager, readSelectorManager)

    private val readHandlers = mutableListOf<ReadHandler>()

    init {
        (0 until READER_SIZE)
            .map { "_READER_${it}_" }
            .forEach { key ->
                readHandlers.add(ReadHandler(key, socketChannelsManager, readSelectorManager))
                readSelectorManager.newSelector(key)
            }

        with(serverSocketChannel) {
            bind(InetSocketAddress(port))
            configureBlocking(false)
            register(acceptHandler.acceptSelector, SelectionKey.OP_ACCEPT)
        }
    }

    fun startup() {
        log("Startup Server on Port[$port]...")
        acceptHandler.startup()
        readHandlers.forEach { it.startup() }

        while (running) {
            sleep(10)
        }

        acceptHandler.shutdown()
        readHandlers.forEach { it.shutdown() }

        log("Server on Port[$port] shut down")
    }
}

fun main() {
    NioNonBlockingMultipleSelectorServer(NIO_NON_BLOCKING_MULTIPLE_SELECTOR_SERVER_PORT).startup()
}