package com.bory.server.netty

import com.bory.server.log
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.util.ReferenceCountUtil

fun main() {
    val byteBuf = Unpooled.buffer(8)
    byteBuf.log("After Initialized")

    byteBuf.writeUtf8("한글")
    byteBuf.log("After write '한글'")

    // automatic expansion
    byteBuf.writeUtf8("도 잘 됩니까?")
    byteBuf.log("After write '도 잘 됩니까?'")

    // read data
    // after read readableBytes will be zero
    val data = byteBuf.readAllUtf8()
    log("READ Data ::: $data")
    byteBuf.log("After read all data")

    // clear byteBuf
    byteBuf.clear()
    byteBuf.log("After clear")

    // rewrite and read
    byteBuf.writeUtf8("다시 씁니다.")
    byteBuf.log("After write '다시 씁니다.'")
    val data2 = byteBuf.readAllUtf8()
    log("READ Data2 ::: $data2")
    byteBuf.log("After read all data")

    // release ByteBuf to pool
    ReferenceCountUtil.release(byteBuf)
    byteBuf.log("After ReferenceCountUtil.release(byteBuf)")

    // reuse of release ByteBuf will throw exception
    byteBuf.writeUtf8("이러면 안 됩니다.")
}

fun ByteBuf.log(message: String? = null) {
    if (message != null) {
        com.bory.server.log("---------------- $message ----------------")
    }
    com.bory.server.log(toString())
    com.bory.server.log("Readable Bytes ::: ${readableBytes()}")
    com.bory.server.log("Writable Bytes ::: ${writableBytes()}")
    com.bory.server.log("Reference Count ::: ${refCnt()}")

    com.bory.server.log("------------------------------------------------------------")
}

fun ByteBuf.writeUtf8(message: String) = this.writeCharSequence(message, Charsets.UTF_8)

fun ByteBuf.readUtf8(length: Int): CharSequence = this.readCharSequence(length, Charsets.UTF_8)

fun ByteBuf.readAllUtf8(): CharSequence = this.readUtf8(this.readableBytes())