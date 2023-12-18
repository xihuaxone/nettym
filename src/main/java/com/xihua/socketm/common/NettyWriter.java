package com.xihua.socketm.common;

import com.xihua.socketm.common.domain.NettyReq;
import com.xihua.socketm.common.domain.NettyResp;
import com.xihua.socketm.common.domain.ReqFuture;
import com.xihua.socketm.common.handler.ChannelManager;
import com.xihua.socketm.common.utils.MsgIdManager;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NettyWriter {
    private final Channel channel;

    private final Logger logger = LoggerFactory.getLogger(NettyWriter.class);

    public NettyWriter() {
        this.channel = ChannelManager.getAll().get(0);
    }

    public NettyWriter(String remoteIp) {
        this.channel = ChannelManager.get(remoteIp);
    }

    public Object call(String api, int timeout, Object... params) {
        NettyReq req = new NettyReq();
        req.setMsgId(MsgIdManager.get());
        req.setApi(api);
        req.setParams(params);
        ReqFuture reqFuture = new ReqFuture();
        req.setReqFuture(reqFuture);

        channel.writeAndFlush(req);

        try {
            NettyResp nettyResp = reqFuture.get(timeout, TimeUnit.SECONDS);
            return nettyResp.getData();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("call api error: {}", e.toString());
        }
        return null;
    }
}
