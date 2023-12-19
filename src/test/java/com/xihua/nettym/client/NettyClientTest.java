package com.xihua.nettym.client;


import com.alibaba.fastjson.JSON;
import com.xihua.nettym.common.NettyWriter;
import com.xihua.nettym.common.handler.ChannelManager;
import com.xihua.nettym.common.handler.ReqHandleHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;

public class NettyClientTest {
    private final Logger logger = LoggerFactory.getLogger(NettyClientTest.class);

    @Test
    public void run() {
        Thread nettyConnListener = new Thread(() -> {
            while (true) {
                if (ChannelManager.getAll().isEmpty()) {
                    try {
                        NettyClient.start("127.0.0.1", 7090);
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
        });

        // 后台开启netty连接监听；
        nettyConnListener.setDaemon(true);
        nettyConnListener.start();

        // 等待netty连接成功；
        while (ChannelManager.getAll().isEmpty()) {
        }
        logger.info("netty client channels = {}", JSON.toJSONString(ChannelManager.getAll()));

        // 注册client端的服务接口，供server端调用；
        ReqHandleHandler.registerReqHandler("testClient", CliHandler.class);

        // 调用server端的接口；
        try {
            Object res = new NettyWriter().call("test", 3, "p1", "p2", "p3");
            logger.info("netty client call resp = {}", JSON.toJSONString(res));
        } catch (ConnectException e) {
            e.printStackTrace();
        }

    }
}
