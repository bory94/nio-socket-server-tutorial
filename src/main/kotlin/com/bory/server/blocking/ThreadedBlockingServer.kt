package com.bory.server.blocking

import com.bory.server.log
import java.net.ServerSocket
import kotlin.concurrent.thread

class ThreadedBlockingServer(port: Int) {
    private val serverSocket: ServerSocket

    init {
        serverSocket = ServerSocket(port)
    }

    fun startup() {
        log("Server Starting...")
        while (true) {
            processBlockingIoRequest(serverSocket) { socket ->
                thread { ClientSocketProcessor(socket).process() }
            }
        }
    }
}

fun main() {
    val server = ThreadedBlockingServer(THREAD_BLOCKING_SERVER_PORT)
    server.startup()
}