package com.xihua.nettym.common.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOLogHandler extends ChannelDuplexHandler {
    private final Logger logger = LoggerFactory.getLogger(IOLogHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("channelRead={}", msg);
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        logger.info("writeable={}, write={}", ctx.channel().isWritable(), msg);
        super.write(ctx, msg, promise);
    }
}
