package com.xihua.nettym.common.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class MsgIdManager {
    private static final AtomicInteger msgId = new AtomicInteger(0);

    public static int get() {
        return msgId.getAndIncrement();
    }
}
