package com.xihua.nettym.common.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class NettyReq {
    /**
     * netty异步调用转同步调用时，需要一个唯一id来给netty请求和响应做配对；
     */
    private int msgId;

    /**
     * 要调用的对端接口；
     */
    private String api;

    /**
     * 调用接口时传入的位置参数；
     */
    private Object[] params;

    /**
     * 实现接口同步调用的核心，请求获得响应时，会把响应对象放到这个future里；
     */
    @JSONField(serialize = false)
    private ReqFuture reqFuture;
}
