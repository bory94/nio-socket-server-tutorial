package com.bory.server.nonblocking.handler

import java.nio.channels.SelectionKey

interface NonblockingHandler {
    fun handle(key: SelectionKey)
}