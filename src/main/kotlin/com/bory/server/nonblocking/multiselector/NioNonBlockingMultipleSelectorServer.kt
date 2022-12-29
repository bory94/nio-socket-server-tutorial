package com.bory.server.nonblocking.multiselector

import com.bory.server.log
import com.bory.server.nonblocking.NIO_NON_BLOCKING_MULTIPLE_SELECTOR_SERVER_PORT
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel

private val CORE_NUMBER = Runtime.getRuntime().availableProcessors()

class NioNonBlockingMultipleSelectorServer(private val port: Int) {
    private val startTime = System.nanoTime()

    @Volatile
    private var running: Boolean = true
    private val serverSocketChannel = ServerSocketChannel.open()
    private val clientSocketChannelsManager = ClientSocketChannelsManager()

    private val readHandlersManager = ReadHandlersManager(clientSocketChannelsManager)
    private val acceptHandler = AcceptHandler(clientSocketChannelsManager, readHandlersManager)

    init {
        log("----------------------------------------------------")
        log("- Starting up NioNonBlockingMultipleSelectorServer -")
        log("----------------------------------------------------")
        log("Initializing ReadHandlersManager by invoking newSelector")
        log("\tCORE_NUMBER is $CORE_NUMBER, so initializing ${CORE_NUMBER * 2} ReadHandlers...")
        (0 until CORE_NUMBER * 2)
            .map { "_READER_${it}_" }
            .forEach { key ->
                readHandlersManager.newSelector(key)
                log("\t==> ReadHandler[$key] initialized")
            }

        log("Initializing ServerSocketChannel")
        with(serverSocketChannel) {
            bind(InetSocketAddress(port))
            configureBlocking(false)
            register(acceptHandler.acceptSelector, SelectionKey.OP_ACCEPT)
        }
        log("ServerSocketChannel initialized")
    }

    fun startup() {
        acceptHandler.startup()
        readHandlersManager.startupAllHandlers()

        log("Server Successfully initialized and listening to client request ::: Port[$port]")
        log("Server Starting up time ::: ${(System.nanoTime() - startTime) / 1_000_000}ms")

        while (running) {
            sleep(10)
        }

        log("Shutting down server...")
        acceptHandler.shutdown()
        readHandlersManager.shutdownAllHandlers()

        log("Server on Port[$port] completely shut down")
    }
}

fun main() {
    NioNonBlockingMultipleSelectorServer(NIO_NON_BLOCKING_MULTIPLE_SELECTOR_SERVER_PORT).startup()
}