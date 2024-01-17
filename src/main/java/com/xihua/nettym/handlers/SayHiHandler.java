package com.xihua.nettym.handlers;

import com.alibaba.fastjson.JSON;
import com.xihua.nettym.common.domain.NettyResp;
import com.xihua.nettym.common.handler.ReqHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SayHiHandler implements ReqHandler {
        @Override
        public NettyResp invoke(Object... params) {
            NettyResp resp = new NettyResp();
            resp.setSuccess(true);
            List<String> stringList = Arrays.stream(params).map(p -> (String) p).collect(Collectors.toList());
            resp.setData("hi you, server receive param = " + JSON.toJSONString(stringList));
            return resp;
        }
    }