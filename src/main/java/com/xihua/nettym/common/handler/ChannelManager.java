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

    public static void put(String remoteAddr, Channel channel) {
        ChannelManager.CHANNEL_MAP.putIfAbsent(remoteAddr, channel);
    }

    public static void remove(String remoteAddr) {
        ChannelManager.CHANNEL_MAP.remove(remoteAddr);
    }
}
