package com.daishuai.netty.demo;

import com.daishuai.netty.handler.ServerInboundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName NettyServerDemo
 * @Author daishuai
 * @Date 2022/3/3 20:52
 * @Version 1.0
 */
@Slf4j
public class NettyServerDemo {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup boosGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boosGroup, workGroup)
                // 创建一个ReflectiveChannelFactory，通过反射创建NioServerSocketChannel
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childAttr(AttributeKey.newInstance("childAttr"), "childAttr")
                .handler(new ServerInboundHandler())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        log.info(">>>>>>>>>>>>>>>>>>>>initChannel");
                    }
                });
        System.out.println(Thread.currentThread());
        ChannelFuture future = bootstrap.bind(9999).sync();
        System.out.println(Thread.currentThread());
        future.channel().closeFuture().sync();
    }
}
