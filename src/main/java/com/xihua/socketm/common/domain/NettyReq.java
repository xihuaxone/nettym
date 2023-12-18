package com.xihua.socketm.common.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class NettyReq {
    private int msgId;

    private String api;

    private Object[] params;

    @JSONField(serialize = false)
    private ReqFuture reqFuture;
}
