package com.xihua.nettym.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultHandler extends SimpleChannelInboundHandler<Object> {
    private final Logger logger = LoggerFactory.getLogger(DefaultHandler.class);

    /**
     * 对于流水线上游未处理/传递下来的数据，手动收尾；
     * @param channelHandlerContext
     * @param o
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        // 目前只有心跳数据会流转到这里，因此不需要做任何处理。注意此方法/类不要删掉，否则数据会流转到netty流水线自带的tailHandler，会有日志警告；
    }
}
