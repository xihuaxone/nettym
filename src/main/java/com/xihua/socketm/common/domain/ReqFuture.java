package com.xihua.socketm.common.domain;

import java.util.concurrent.FutureTask;

public class ReqFuture extends FutureTask<NettyResp> {
    private final long expireAt;

    public ReqFuture() {
        this(1000 * 10);
    }

    public ReqFuture(int timeout) {
        super(() -> null);
        expireAt = System.currentTimeMillis() + timeout;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireAt;
    }

    public void setResp(NettyResp resp) {
        super.set(resp);
    }
}
