package com.bory.server.blocking

import com.bory.server.log
import java.io.BufferedWriter
import java.net.ServerSocket
import java.net.Socket

const val MAIN_BLOCKING_SERVER_PORT = 9999
const val THREAD_BLOCKING_SERVER_PORT = 9998
const val THREAD_POOL_BLOCKING_SERVER_PORT = 9997

fun processBlockingIoRequest(serverSocket: ServerSocket, handleFunc: (Socket) -> Unit) {
    log("Ready for accept client socket...")
    val socket = serverSocket.accept()
    log("Client socket accepted")
    handleFunc(socket)

}

fun writeBlockingMessage(writer: BufferedWriter, message: String, newLine: Boolean = true) =
    try {
        writer.write(message)
        if (newLine) {
            writer.newLine()
        }
        writer.flush()
    } catch (e: Exception) {
        log("Writer Error: ${e.message}")
    }