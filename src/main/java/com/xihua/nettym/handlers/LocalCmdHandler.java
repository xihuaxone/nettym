package com.xihua.nettym.handlers;

import com.alibaba.fastjson.JSON;
import com.xihua.nettym.common.domain.NettyResp;
import com.xihua.nettym.common.handler.ReqHandler;
import com.xihua.nettym.utils.LocalCmdUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LocalCmdHandler implements ReqHandler {
        @Override
        public NettyResp invoke(Object... params) {
            NettyResp resp = new NettyResp();

            String cmd = (String) params[0];
            String res;
            try {
                res = LocalCmdUtil.run(cmd);
                resp.setSuccess(true);
                resp.setData(res);
            } catch (Exception e) {
                resp.setSuccess(false);
                resp.setErrMsg(e.toString());
            }
            return resp;
        }
    }