package com.bory.server.nonblocking.selector.handler

import java.nio.channels.SelectionKey

interface NonblockingHandler {
    fun handle(key: SelectionKey)
}