package com.daishuai.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName ServerInboundHandler
 * @Author daishuai
 * @Date 2022/3/3 20:58
 * @Version 1.0
 */
@Slf4j
public class ServerInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("ServerHandler channelActive >>>>>>>>>>>");
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("ServerHandler channelRegistered >>>>>>>>>>>");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("ServerHandler handlerAdded >>>>>>>>>>>");
    }
}
