package com.xihua.nettym.client;

import com.xihua.nettym.common.handler.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

public class NettyClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    public static void start(String host, int port) throws InterruptedException, ConnectException {

        NioEventLoopGroup main = new NioEventLoopGroup(1);


        ChannelInitializer<SocketChannel> initializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline()
                        .addLast(new LengthFieldBasedFrameDecoder(1024*4, 0, 4, 0, 4))
                        .addLast(new LengthFieldPrepender(4))
                        .addLast(new ByteBufEncoder())
                        .addLast(new ByteBufDecoder())
                        .addLast(new IOLogHandler())
                        .addLast(new IdleStateHandler(0L,0L,5L, TimeUnit.SECONDS))
                        .addLast()
                        .addLast(new ChannelInitHandler())
                        .addLast(new ReqHandleHandler())
                        .addLast(new ReqInvokeHandler())
                ;
            }
        };

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(main)
                .channel(NioSocketChannel.class)
                .handler(initializer);

        ChannelFuture initFuture = bootstrap.connect(host, port).sync();

        initFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(Future<? super Void> future) throws Exception {
                logger.info("client init finished: {}", future.isSuccess());
            }
        });
    }
}
