package com.xihua.nettym.common.handler;

import com.xihua.nettym.common.domain.NettyResp;

public interface ReqHandler {
    NettyResp invoke(Object... params);
}
