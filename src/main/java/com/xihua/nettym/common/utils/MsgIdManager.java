package com.xihua.nettym.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class MsgIdManager {
    private static final AtomicInteger msgId = new AtomicInteger(0);
    private static final Logger logger = LoggerFactory.getLogger(MsgIdManager.class);
    private static final int MAX_SAFE_ID = Integer.MAX_VALUE - 1000; // 提前重置，避免溢出

    /**
     * 获取下一个消息 ID
     * 当接近 Integer.MAX_VALUE 时会自动重置，避免溢出
     * @return 消息 ID
     */
    public static int get() {
        int current = msgId.getAndIncrement();
        // 检查是否接近溢出，如果是则重置
        if (current >= MAX_SAFE_ID) {
            // 使用 CAS 操作确保只有一个线程执行重置
            if (msgId.compareAndSet(current + 1, 0)) {
                logger.warn("MsgId reached safe limit, resetting to 0. Previous value: {}", current);
            }
            // 如果重置失败，说明其他线程已经重置，继续使用当前值
            // 由于使用了 CAS，这里返回的值仍然有效
        }
        return current;
    }
}
