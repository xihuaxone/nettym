package com.xihua.nettym.server;

import com.xihua.nettym.common.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {
    private static final int port = 7090;

    private static final NioEventLoopGroup reqHandleGroup = new NioEventLoopGroup(64);

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    public static void start() throws InterruptedException {
        NioEventLoopGroup main = new NioEventLoopGroup(1);
        NioEventLoopGroup slave = new NioEventLoopGroup(64);

        ChannelInitializer<SocketChannel> initializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline()
                        .addLast(new LengthFieldBasedFrameDecoder(1024*4, 0, 4, 0, 4))
                        .addLast(new LengthFieldPrepender(4))
                        .addLast(new ByteBufEncoder())
                        .addLast(new ByteBufDecoder())
                        .addLast(new IOLogHandler())
                        .addLast(new ChannelInitHandler())
                        .addLast(new ReqHandleHandler())
                        .addLast(new ReqInvokeHandler())
                ;
            }
        };

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(main, slave)
                .channel(NioServerSocketChannel.class)
                .childHandler(initializer);

        ChannelFuture initFuture = bootstrap.bind(port).sync();

        initFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
            public void operationComplete(Future<? super Void> future) throws Exception {
                logger.info("server init finished: {}", future.isSuccess());
            }
        });
    }
}
