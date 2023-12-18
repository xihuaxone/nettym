package com.xihua.nettym.common.handler;

import com.xihua.nettym.common.domain.NettyReq;
import com.xihua.nettym.common.domain.NettyResp;
import com.xihua.nettym.common.domain.ReqFuture;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ReqInvokeHandler extends ChannelDuplexHandler {
    private static final Map<Integer, ReqFuture> REQ_FUTURE_MAP = new ConcurrentHashMap<Integer, ReqFuture>(50);

    private static final int FUTURE_RECYCLE_WATER = 50;

    private final Logger logger = LoggerFactory.getLogger(ReqInvokeHandler.class);

    private void doRequest(ChannelHandlerContext ctx, NettyReq req) {
        ctx.writeAndFlush(req);
        REQ_FUTURE_MAP.put(req.getMsgId(), req.getReqFuture());

        // 达到水位则回收过期Future；
        if (REQ_FUTURE_MAP.size() >= FUTURE_RECYCLE_WATER) {
            recycleExpiredFutures();
        }
    }

    private void doRequestCallback(ChannelHandlerContext ctx, NettyResp resp) {
        ReqFuture reqFuture = REQ_FUTURE_MAP.remove(resp.getMsgId());
        if (reqFuture == null) {
            logger.warn("msgId respFuture lost: {}", resp.getMsgId());
            return;
        }
        reqFuture.setResp(resp);
    }

    private void recycleExpiredFutures() {
        List<Integer> expiredKeyList = REQ_FUTURE_MAP.entrySet().stream()
                .filter(e -> e.getValue().isExpired())
                .map(Map.Entry::getKey).collect(Collectors.toList());
        for (Integer k : expiredKeyList) {
            REQ_FUTURE_MAP.remove(k);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof NettyResp) {
            doRequestCallback(ctx, (NettyResp) msg);
            return;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof NettyReq) {
            doRequest(ctx, (NettyReq) msg);
            return;
        }
        super.write(ctx, msg, promise);
    }
}
