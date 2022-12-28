package com.bory.server.blocking

import com.bory.server.log
import java.net.ServerSocket

class MainBlockingServer(port: Int) {
    private val serverSocket: ServerSocket = ServerSocket(port)

    fun startup() {
        log("Server Starting...")
        while (true) {
            processBlockingIoRequest(serverSocket) { socket -> ClientSocketProcessor(socket).process() }
        }
    }
}

fun main() {
    val server = MainBlockingServer(MAIN_BLOCKING_SERVER_PORT)
    server.startup()
}