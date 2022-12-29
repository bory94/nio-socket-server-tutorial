package com.bory.server.blocking

import com.bory.server.log
import java.net.ServerSocket
import java.util.concurrent.Executors

class ThreadPooledBlockingServer(private val port: Int) {
    private val serverSocket = ServerSocket(port)
    private val executor = Executors.newFixedThreadPool(3)

    fun startup() {
        log("Startup Server on Port[$port]...")
        while (true) {
            processBlockingIoRequest(serverSocket) { socket ->
                executor.submit {
                    ClientSocketProcessor(socket).process()
                }
            }
        }
    }
}

fun main() {
    ThreadPooledBlockingServer(THREAD_POOL_BLOCKING_SERVER_PORT).startup()
}