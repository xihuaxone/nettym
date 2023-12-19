package com.xihua.nettym.common.handler;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {
    private static final Map<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>(20);

    private final Logger logger = LoggerFactory.getLogger(ChannelManager.class);

    public static List<Channel> getAll() {
        return new ArrayList<>(CHANNEL_MAP.values());
    }

    public static Channel get(String remoteIp) {
        return CHANNEL_MAP.getOrDefault(remoteIp, null);
    }

    /**
     * 主动关闭channel，netty会自动触发remove方法来删除CHANNEL_MAP中的该channel；
     * @param channel
     */
    public static void disconnect(Channel channel) {
        channel.closeFuture();
    }

    protected static void put(String remoteAddr, Channel channel) {
        synchronized (CHANNEL_MAP) {
            ChannelManager.CHANNEL_MAP.putIfAbsent(remoteAddr, channel);
        }
    }

    protected static void remove(String remoteAddr) {
        synchronized (CHANNEL_MAP) {
            ChannelManager.CHANNEL_MAP.remove(remoteAddr);
        }
    }
}
