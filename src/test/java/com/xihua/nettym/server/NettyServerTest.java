package com.xihua.nettym.server;


import com.xihua.nettym.common.NettyWriter;
import com.xihua.nettym.common.handler.ChannelManager;
import com.xihua.nettym.common.handler.ReqHandleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class NettyServerTest {
    private final Logger logger = LoggerFactory.getLogger(NettyServerTest.class);

    private void startServer() throws InterruptedException {
        ReqHandleHandler.registerReqHandler("testServer", ServerHandler.class);
        NettyServer.start();
    }

    @org.junit.Test
    public void test() throws InterruptedException {
        startServer();

        Random random = new Random();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(64, 64,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        List<Future<Object>> futureList = new ArrayList<>(100);

        while (ChannelManager.get("127.0.0.1") == null) {
        }
        logger.info("client connected.");

        for (int i = 0; i < 10; i++) {
            Callable<Object> runnable = () -> {
                Object res = new NettyWriter("127.0.0.1").call("testClient", 3, "p1", "p2", "p3");
                logger.info("call res={}", res);
                return res;
            };
            Future<Object> future = executor.submit(runnable);
            futureList.add(future);
            Thread.sleep(random.nextInt(500));
        }

        for (Future<Object> future : futureList) {
            try {
                Object o = future.get(3, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                logger.error("error: " + e);
            }
        }

        while (true) {
            Thread.sleep(1000);
        }
    }
}
