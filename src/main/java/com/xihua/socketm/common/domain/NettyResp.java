package com.xihua.socketm.common.domain;

import lombok.Data;

@Data
public class NettyResp {
    private int msgId;
    private boolean success;
    private String errMsg;
    private Object data;
}
