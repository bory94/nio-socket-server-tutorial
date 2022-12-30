package com.bory.server.netty.simple

import com.bory.server.log
import com.bory.server.netty.readAllUtf8
import com.bory.server.netty.writeUtf8
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

class NettyServer(private val port: Int) {
    fun startup() {
        val eventLoopGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
                .group(eventLoopGroup)
                .channel(NioServerSocketChannel::class.java)
                .localAddress(port)
                .childHandler(EchoChannelInitializer())

            val future = bootstrap.bind().sync()
            println("Server Started in Port [$port]")

            future.channel().closeFuture().sync()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            eventLoopGroup.shutdownGracefully().sync()
        }
    }
}

class EchoChannelInitializer : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().addLast(NettyEchoHandler())
    }
}

@ChannelHandler.Sharable
class NettyEchoHandler : ChannelInboundHandlerAdapter() {
    override fun channelRegistered(ctx: ChannelHandlerContext) {
        super.channelRegistered(ctx)

        val welcomeMessage = "Welcome! $ctx!\n> "
        val writeBuf = Unpooled.buffer(welcomeMessage.length)
        writeBuf.writeUtf8(welcomeMessage)

        ctx.writeAndFlush(writeBuf)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        log("Channel Read")

        val byteBuf = msg as ByteBuf
        val readMessage = byteBuf.readAllUtf8().trim()
        val returnMessage = "<== Read Data = $readMessage"
        log(returnMessage)
        val writeBuf = Unpooled.buffer(returnMessage.length + 4)
        writeBuf.writeUtf8("$returnMessage\n> ")

        ctx.writeAndFlush(writeBuf)

        if (readMessage == "exit") {
            ctx.close()
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
            .addListener { ChannelFutureListener.CLOSE }
    }
}

fun main() {
    NettyServer(8080).startup()
}