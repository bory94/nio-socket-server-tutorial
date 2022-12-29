package com.bory.server.nonblocking.multiselector

import java.nio.channels.Selector
import java.util.concurrent.ConcurrentHashMap

class ReadSelectorsManager {
    private val readSelectorsMap = ConcurrentHashMap<String, Selector>()

    fun newSelector(key: String) {
        if (readSelectorsMap.containsKey(key)) {
            return
        }

        readSelectorsMap[key] = Selector.open()
    }

    fun selector(key: String): Selector {
        if (!readSelectorsMap.containsKey(key)) {
            newSelector(key)
        }

        return readSelectorsMap[key]!!
    }

    fun keys() = readSelectorsMap.keys.toList()
}