package com.bory.server.netty.simple

import com.bory.server.log
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel

class NettyClient(private val host: String, private val port: Int) {
    fun startup() {
        val eventLoopGroup = NioEventLoopGroup()
        try {
            val bootstrap = Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel::class.java)
                .remoteAddress(host, port)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline().addLast(EchoClientHandler())
                    }
                })

            val future = bootstrap.connect().sync()
            future.channel().closeFuture().sync()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            eventLoopGroup.shutdownGracefully().sync()
        }
    }
}

class EchoClientHandler : SimpleChannelInboundHandler<ByteBuf>() {
    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(Unpooled.copiedBuffer("잘 될까요?", Charsets.UTF_8))
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
        log("<== Client Received Message: ${msg.toString(Charsets.UTF_8)}")
        ctx.close()
    }
}

fun main() {
    NettyClient("localhost", 8080).startup()
}