package com.xihua.nettym;

import com.xihua.nettym.common.handler.ReqHandleHandler;
import com.xihua.nettym.handlers.SayHiHandler;
import com.xihua.nettym.server.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        // java -jar mqttm-jar-with-dependencies.jar port=8080

        Map<String, String> option = new HashMap<>(args.length);
        for (String arg : args) {
            String[] kv = arg.split("=");
            option.put(kv[0].trim(), kv[1].trim());
        }

        int port = Integer.parseInt(option.getOrDefault("port", "3090"));

        ReqHandleHandler.registerReqHandler("sayHi", SayHiHandler.class);
        logger.info("start server with port={}", port);
        NettyServer.start(port);
    }
}