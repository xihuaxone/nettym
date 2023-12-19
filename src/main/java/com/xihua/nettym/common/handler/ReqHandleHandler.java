package com.xihua.nettym.common.handler;

import com.xihua.nettym.common.domain.NettyReq;
import com.xihua.nettym.common.domain.NettyResp;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ReqHandleHandler extends SimpleChannelInboundHandler<NettyReq> {
    private static final Map<String, Class<?>> REQ_HANDLER_MAP = new HashMap<>(50);

    // 执行业务处理的线程池，需要保证全局共享；
    private static final ExecutorService REQ_HANDLER_EXECUTOR = new ThreadPoolExecutor(64, 64,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    public static void registerReqHandler(String api, Class<?> reqHandler) {
        boolean implemented = false;
        for (Class<?> i : reqHandler.getInterfaces()) {
            if (i.equals(ReqHandler.class)) {
                implemented = true;
                break;
            }
        }
        if (!implemented) {
            throw new RuntimeException("expect implement of " + ReqHandler.class.getName());
        }
        REQ_HANDLER_MAP.put(api, reqHandler);
    }

    protected void channelRead0(ChannelHandlerContext ctx, NettyReq req) throws Exception {
        Class<?> reqHandler = REQ_HANDLER_MAP.get(req.getApi());
        if (reqHandler == null) {
            NettyResp resp = new NettyResp();
            resp.setMsgId(req.getMsgId());
            resp.setSuccess(false);
            resp.setErrMsg("api not found.");
            ctx.writeAndFlush(resp);
            return;
        }

        // 获取handler实例对象；
        ReqHandler handlerImpl = (ReqHandler) reqHandler.getDeclaredConstructor().newInstance();

        // 异步调用handler实例执行业务处理，不阻塞主线程；
        REQ_HANDLER_EXECUTOR.execute(() -> {
            NettyResp resp = handlerImpl.invoke(req.getParams());
            if (resp == null) {
                resp = new NettyResp();
                resp.setSuccess(false);
                resp.setErrMsg("handler invoke response is null.");
            }
            resp.setMsgId(req.getMsgId());
            ctx.writeAndFlush(resp);
        });
    }
}
