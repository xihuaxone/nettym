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

    private static final int FUTURE_RECYCLE_WATER = 100;
    private static final int FUTURE_RECYCLE_INTERVAL = 30; // 30秒定期清理一次

    private final Logger logger = LoggerFactory.getLogger(ReqInvokeHandler.class);

    static {
        // 启动定期清理过期 Future 的线程
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "FutureRecycleScheduler");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(ReqInvokeHandler::recycleExpiredFutures, 
                FUTURE_RECYCLE_INTERVAL, FUTURE_RECYCLE_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 发起请求前的处理；
     * @param ctx
     * @param req
     */
    private void doRequest(ChannelHandlerContext ctx, NettyReq req) {
        ctx.writeAndFlush(req);

        // 把请求future存储起来；
        REQ_FUTURE_MAP.put(req.getMsgId(), req.getReqFuture());

        // 控制请求并发量，达到水位则回收过期Future；
        if (REQ_FUTURE_MAP.size() >= FUTURE_RECYCLE_WATER) {
            recycleExpiredFutures();
        }
    }

    /**
     * 收到响应时对响应对象的处理；
     * @param ctx
     * @param resp
     */
    private void doRequestCallback(ChannelHandlerContext ctx, NettyResp resp) {
        ReqFuture reqFuture = REQ_FUTURE_MAP.remove(resp.getMsgId());
        if (reqFuture == null) {
            logger.warn("msgId respFuture lost: {}", resp.getMsgId());
            return;
        }
        // 把响应对象推送到该请求的future中；
        reqFuture.setResp(resp);
    }

    /**
     * 清理过期的 Future，防止内存泄漏
     */
    private static void recycleExpiredFutures() {
        List<Integer> expiredKeyList = REQ_FUTURE_MAP.entrySet().stream()
                .filter(e -> e.getValue().isExpired())
                .map(Map.Entry::getKey).collect(Collectors.toList());
        for (Integer k : expiredKeyList) {
            ReqFuture future = REQ_FUTURE_MAP.remove(k);
            if (future != null && !future.isDone()) {
                // 为过期的 Future 设置超时响应
                NettyResp timeoutResp = new NettyResp();
                timeoutResp.setMsgId(k);
                timeoutResp.setSuccess(false);
                timeoutResp.setErrMsg("Request timeout");
                future.setResp(timeoutResp);
            }
        }
        if (!expiredKeyList.isEmpty()) {
            LoggerFactory.getLogger(ReqInvokeHandler.class)
                    .debug("Recycled {} expired futures", expiredKeyList.size());
        }
    }

    /**
     * Channel 断开时清理所有相关的 Future
     * @param remoteAddr 远程地址
     */
    public static void onChannelRemoved(String remoteAddr) {
        // 清理所有过期的 Future（Channel 断开时，所有未完成的请求都应该被视为失败）
        recycleExpiredFutures();
        // 清理所有未完成的 Future
        List<Integer> allKeys = new java.util.ArrayList<>(REQ_FUTURE_MAP.keySet());
        for (Integer k : allKeys) {
            ReqFuture future = REQ_FUTURE_MAP.remove(k);
            if (future != null && !future.isDone()) {
                NettyResp errorResp = new NettyResp();
                errorResp.setMsgId(k);
                errorResp.setSuccess(false);
                errorResp.setErrMsg("Channel disconnected");
                future.setResp(errorResp);
            }
        }
        LoggerFactory.getLogger(ReqInvokeHandler.class)
                .info("Cleaned up futures for disconnected channel: {}", remoteAddr);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Channel 断开时清理相关 Future
        String remoteAddr = ctx.channel().remoteAddress() != null 
                ? ctx.channel().remoteAddress().toString() : "unknown";
        onChannelRemoved(remoteAddr);
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof NettyResp) {
            doRequestCallback(ctx, (NettyResp) msg);
            return;
        }
        // 其他类型的响应对象，放行到后续流水线；
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof NettyReq) {
            doRequest(ctx, (NettyReq) msg);
            return;
        }
        // 其他类型的请求对象，放行到后续流水线；
        super.write(ctx, msg, promise);
    }
}
