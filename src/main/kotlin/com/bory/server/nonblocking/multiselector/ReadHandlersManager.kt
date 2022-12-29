package com.bory.server.nonblocking.multiselector

import com.bory.server.log
import java.nio.channels.Selector
import java.util.concurrent.ConcurrentHashMap

class ReadHandlersManager(
    private val clientSocketChannelsManager: ClientSocketChannelsManager
) {
    private val readSelectorsMap = ConcurrentHashMap<String, Selector>()
    private val readHandlers = mutableListOf<ReadHandler>()

    fun newSelector(key: String) {
        if (readSelectorsMap.containsKey(key)) {
            return
        }

        Selector.open().apply {
            readSelectorsMap[key] = this
            readHandlers.add(ReadHandler(key, clientSocketChannelsManager, this))
        }
    }

    fun selector(key: String): Selector {
        if (!readSelectorsMap.containsKey(key)) {
            newSelector(key)
        }

        return readSelectorsMap[key]!!
    }

    fun keys() = readSelectorsMap.keys.toList()

    fun startupAllHandlers() {
        log("Starting up All ReadHandlers")
        readHandlers.forEach { readHandler ->
            log("\t==> Starting ReadHandler[${readHandler.readerKey}]")
            readHandler.startup()
            log("\t==> ReadHandler[${readHandler.readerKey}] started.")
        }
    }

    fun shutdownAllHandlers() {
        log("Shutting down All ReadHandlers")
        readHandlers.forEach { readHandler ->
            log("\t==> Shutting down ReadHandler[${readHandler.readerKey}]")
            readHandler.shutdown()
            log("\t==> ReadHandler[${readHandler.readerKey}] shut down")
        }
    }
}