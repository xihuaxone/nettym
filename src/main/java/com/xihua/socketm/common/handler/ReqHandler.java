package com.xihua.socketm.common.handler;

import com.xihua.socketm.common.domain.NettyResp;

public interface ReqHandler {
    NettyResp invoke(Object... params);
}
