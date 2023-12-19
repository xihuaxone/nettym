package com.xihua.nettym.server;


import com.xihua.nettym.common.NettyWriter;
import com.xihua.nettym.common.handler.ChannelManager;
import com.xihua.nettym.common.handler.ReqHandleHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;

public class NettyServerTest {
    private final Logger logger = LoggerFactory.getLogger(NettyServerTest.class);

    @Test
    public void run() throws InterruptedException {
        // 注册server端的服务接口，供client端调用；
        ReqHandleHandler.registerReqHandler("testServer", ServerHandler.class);

        // 启动netty server；
        NettyServer.start(7090);

        // 轮询等待要调用的目标client连接上；
        while (ChannelManager.get("127.0.0.1") == null) {
        }

        // 调用client的接口；
        try {
            Object res = new NettyWriter("127.0.0.1").call("testClient", 3, "p1", "p2", "p3");
            logger.info("call res={}", res);
        } catch (ConnectException e) {
            e.printStackTrace();
        }

        while (true) {
            Thread.sleep(1000);
        }
    }
}
