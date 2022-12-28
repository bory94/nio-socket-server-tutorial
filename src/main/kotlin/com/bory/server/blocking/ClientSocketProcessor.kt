package com.bory.server.blocking

import com.bory.server.log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class ClientSocketProcessor(private val socket: Socket) {
    private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    private val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

    fun process() {
        log("Client socket accepted::: Socket=[$socket]")
        writeBlockingMessage(writer, ">>> You Are Accepted...")
        try {

            val messageBuilder = StringBuilder()

            var line: String? = ""
            while (line != null) {
                writeBlockingMessage(writer, "\nEnter Message... \n> ", false)

                line = reader.readLine()
                log("\t==> $line")
                messageBuilder.append(line.trim()).append("\n")

                writeBlockingMessage(writer, "Received Message: $line")

                if (line.uppercase() == "EXIT") {
                    writeBlockingMessage(writer, "Communication Finished.")
                    break
                }
            }

            writer.use {
                writeBlockingMessage(it, "ECHO Full Text: [\n$messageBuilder\n]")
            }
        } catch (e: Exception) {
            log("Exception: ${e.message ?: "Unknown Error"}")
        } finally {
            try {
                reader.close()
            } catch (e: Exception) {
                log("Reader Close Failed: ${e.message}")
            }
            try {
                writer.close()
            } catch (e: Exception) {
                log("Writer Close Failed: ${e.message}")
            }
        }
    }
}