package com.xihua.nettym.common;

import com.xihua.nettym.common.domain.NettyReq;
import com.xihua.nettym.common.domain.NettyResp;
import com.xihua.nettym.common.domain.ReqFuture;
import com.xihua.nettym.common.handler.ChannelManager;
import com.xihua.nettym.common.utils.MsgIdManager;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NettyWriter {
    private Channel channel;

    private final Logger logger = LoggerFactory.getLogger(NettyWriter.class);

    public NettyWriter() {
        initChannel();
    }

    private void initChannel() {
        List<Channel> channelList = ChannelManager.getAll();
        channelList.stream().filter(Channel::isActive).findAny().ifPresent(value -> this.channel = value);
    }

    public NettyWriter(String remoteIp) {
        this.channel = ChannelManager.get(remoteIp);
    }

    public Object call(String api, int timeout, Object... params) throws ConnectException {
        NettyReq req = new NettyReq();
        req.setMsgId(MsgIdManager.get());
        req.setApi(api);
        req.setParams(params);
        ReqFuture reqFuture = new ReqFuture();
        req.setReqFuture(reqFuture);
        precheck();
        channel.writeAndFlush(req);

        try {
            NettyResp nettyResp = reqFuture.get(timeout, TimeUnit.SECONDS);
            return nettyResp.getData();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("call api error: {}", e.toString());
        }
        return null;
    }

    private void precheck() throws ConnectException {
        // channel不可用时，主动关闭该channel，且5秒内重试5次获取可用channel，还获取不到则抛异常；
        if (channel == null || !channel.isActive()) {
            if (channel != null) {
                ChannelManager.disconnect(channel);
            }
            for (int i = 0; i < 5; i++) {
                initChannel();
                if (this.channel != null && this.channel.isActive()) {
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            throw new ConnectException("channel inactivated.");
        }
    }
}
