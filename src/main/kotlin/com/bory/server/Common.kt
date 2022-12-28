package com.bory.server

import java.time.LocalDateTime

fun log(message: String) {
    println("${LocalDateTime.now()} ::: ${Thread.currentThread().name} ::: $message")
}

