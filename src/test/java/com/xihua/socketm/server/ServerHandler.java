package com.xihua.socketm.server;

import com.alibaba.fastjson.JSON;
import com.xihua.socketm.common.domain.NettyResp;
import com.xihua.socketm.common.handler.ReqHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerHandler implements ReqHandler {
    @Override
    public NettyResp invoke(Object... params) {
        NettyResp resp = new NettyResp();
        resp.setSuccess(true);
        List<String> stringList = Arrays.stream(params).map(p -> (String) p).collect(Collectors.toList());
        resp.setData("server receive param = " + JSON.toJSONString(stringList));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resp;
    }
}
