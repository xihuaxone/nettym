package com.xihua.socketm.common.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class ChannelInitHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(ChannelInitHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        super.channelActive(ctx);
        String remoteAddr = getRemoteAddr(ctx);
        ChannelManager.put(remoteAddr, channel);
        logger.info("channel active. {}", remoteAddr);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String remoteAddr = getRemoteAddr(ctx);
        logger.info("channel inactive. {}", remoteAddr);
        ChannelManager.remove(remoteAddr);
        super.channelInactive(ctx);
    }

    private String getRemoteAddr(ChannelHandlerContext ctx) {
        InetSocketAddress addr = (InetSocketAddress) ctx.channel().remoteAddress();
        return addr.getAddress().getHostAddress();
    }
}
