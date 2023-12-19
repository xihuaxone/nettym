package com.xihua.nettym.client;


import com.alibaba.fastjson.JSON;
import com.xihua.nettym.common.NettyWriter;
import com.xihua.nettym.common.handler.ChannelManager;
import com.xihua.nettym.common.handler.ReqHandleHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class NettyClientCocurrentTest {
    private final Logger logger = LoggerFactory.getLogger(NettyClientCocurrentTest.class);

    private void startClient() throws InterruptedException {
        ReqHandleHandler.registerReqHandler("testClient", CliHandler.class);

        while (ChannelManager.getAll().isEmpty()) {
            try {
                NettyClient.start("127.0.0.1", 7090);
            } catch (ConnectException e) {
                logger.warn("can not connect with server: {}, retry.", e.toString());
            }
            Thread.sleep(2 * 1000);
        }
    }

    @Test
    public void test() throws InterruptedException {
        startClient();

        Random random = new Random();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(64, 64,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        List<Future<Object>> futureList = new ArrayList<>(100);

        while (ChannelManager.get("127.0.0.1") == null) {
        }
        logger.info("server connected.");

        for (int i = 0; i < 10; i++) {
            Callable<Object> runnable = () -> {
                Object res = new NettyWriter("127.0.0.1").call("testServer", 3, "p1", "p2", "p3");
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

        logger.info("finished.");
        while (true) {
            Thread.sleep(1000);
        }
    }
}
