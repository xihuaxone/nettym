package com.xihua.nettym.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static io.netty.handler.timeout.IdleState.ALL_IDLE;

public class IdleStateProcessHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if(evt instanceof IdleStateEvent) {

            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(ALL_IDLE)) {
                InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().localAddress();
                ctx.writeAndFlush("client [" + socketAddress.getAddress().getHostAddress() + "] alive");
            }
        }
    }
}