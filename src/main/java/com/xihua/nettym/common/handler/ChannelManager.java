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
    private static final Logger logger = LoggerFactory.getLogger(ChannelManager.class);

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
        if (channel != null && channel.isActive()) {
            channel.close();
            logger.info("Channel disconnected: {}", channel.remoteAddress());
        }
    }

    protected static void put(String remoteAddr, Channel channel) {
        // ConcurrentHashMap 本身是线程安全的，不需要额外的同步
        ChannelManager.CHANNEL_MAP.putIfAbsent(remoteAddr, channel);
    }

    protected static void remove(String remoteAddr) {
        // ConcurrentHashMap 本身是线程安全的，不需要额外的同步
        Channel removed = ChannelManager.CHANNEL_MAP.remove(remoteAddr);
        if (removed != null) {
            logger.info("Channel removed from manager: {}", remoteAddr);
            // 通知 ReqInvokeHandler 清理该 Channel 相关的 Future
            ReqInvokeHandler.onChannelRemoved(remoteAddr);
        }
    }
}
