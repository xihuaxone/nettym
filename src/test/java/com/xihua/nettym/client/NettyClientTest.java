package com.xihua.nettym.client;


import com.alibaba.fastjson.JSON;
import com.xihua.nettym.common.NettyWriter;
import com.xihua.nettym.common.handler.ChannelManager;
import com.xihua.nettym.common.handler.ReqHandleHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.concurrent.Callable;

public class NettyClientTest {
    private final Logger logger = LoggerFactory.getLogger(NettyClientTest.class);

    private final Runnable nettyConnListener = () -> {
        while (true) {
            if (ChannelManager.getAll().isEmpty()) {
                try {
                    NettyClient.start("127.0.0.1", 8080);
                } catch (ConnectException e) {
                    logger.warn("can not connect with server: {}, retry.", e.toString());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    };

    @Test
    public void run() throws InterruptedException {
        // 注册client端的服务接口，供server端调用；
        ReqHandleHandler.registerReqHandler("testClient", CliHandler.class);

        // 后台开启netty连接监听；
        Thread listerThread = new Thread(nettyConnListener);
        listerThread.setDaemon(true);
        listerThread.start();

        // 等待netty连接成功；
        while (ChannelManager.getAll().isEmpty()) {
        }
        logger.info("netty client channels = {}", JSON.toJSONString(ChannelManager.getAll()));

        // 调用server端的接口；
        try {
            Object res = new NettyWriter().call("sayHi", 3, "p1", "p2", "p3");
            logger.info("netty client call res = {}", res);
        } catch (ConnectException e) {
            e.printStackTrace();
        }

        // hold主线程，供server测试用；
        while (true) {
            Thread.sleep(1000);
        }
    }
}
