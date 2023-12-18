package com.xihua.nettym.server.handler;

import com.xihua.nettym.common.domain.NettyResp;
import com.xihua.nettym.common.handler.ReqHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DemoHandler implements ReqHandler {
    @Override
    public NettyResp invoke(Object... params) {
        NettyResp resp = new NettyResp();
        resp.setSuccess(true);
        List<String> stringList = Arrays.stream(params).map(p -> (String) p).collect(Collectors.toList());
        resp.setData(String.join(", ", stringList));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resp;
    }
}
