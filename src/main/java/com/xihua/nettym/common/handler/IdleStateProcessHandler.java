package com.xihua.nettym.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static io.netty.handler.timeout.IdleState.ALL_IDLE;

public class IdleStateProcessHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(IdleStateProcessHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(ALL_IDLE)) {
                // 发送心跳消息，使用字符串格式（会被编码器识别为 DEFAULT 类型）
                // 心跳消息会被 DefaultHandler 处理，不需要响应
                InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().localAddress();
                String heartbeatMsg = "HEARTBEAT:" + socketAddress.getAddress().getHostAddress();
                if (ctx.channel().isActive()) {
                    ctx.writeAndFlush(heartbeatMsg);
                    logger.debug("Sent heartbeat: {}", heartbeatMsg);
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}