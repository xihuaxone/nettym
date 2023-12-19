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
    private static final NioEventLoopGroup reqHandleGroup = new NioEventLoopGroup(64);

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    public static void start(int port) throws InterruptedException {
        NioEventLoopGroup main = new NioEventLoopGroup(1);
        NioEventLoopGroup slave = new NioEventLoopGroup(64);

        ChannelInitializer<SocketChannel> initializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline()
                        .addLast(new LengthFieldBasedFrameDecoder(1024 * 4, 0, 4, 0, 4))  // 数据报文动态识别，防止沾包、拆包问题；
                        .addLast(new LengthFieldPrepender(4))
                        .addLast(new ByteBufEncoder())  // response编码器，把要发送的数据编码为二进制流；
                        .addLast(new ByteBufDecoder())  // request解码器，把接收的二进制流解码为java对象；
                        .addLast(new IOLogHandler())  // 对网络请求做日志记录；
                        .addLast(new ChannelInitHandler())  // 对channel的生命周期实现感知和管理；
                        .addLast(new ReqHandleHandler())  // 对收到的请求，做处理；
                        .addLast(new ReqInvokeHandler())  // 对要发送出去的请求，做处理；
                        .addLast(new DefaultHandler())
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
