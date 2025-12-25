package com.xihua.nettym.common.handler;

import com.alibaba.fastjson.JSON;
import com.xihua.nettym.common.domain.NettyReq;
import com.xihua.nettym.common.domain.NettyResp;
import com.xihua.nettym.common.enums.MsgTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

public class ByteBufDecoder extends ByteToMessageDecoder {
    private static final int MIN_FRAME_LENGTH = 5; // 1 byte (msgType) + 4 bytes (msgLength)
    private static final int MAX_FRAME_LENGTH = 1024 * 1024; // 1MB 最大消息长度

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 检查是否有足够的数据读取消息类型和长度
        if (in.readableBytes() < MIN_FRAME_LENGTH) {
            return; // 数据不足，等待更多数据
        }

        // 标记读取位置，以便在数据不足时回退
        in.markReaderIndex();

        try {
            byte msgType = in.readByte();
            int msgLength = in.readInt();

            // 验证消息长度
            if (msgLength < 0 || msgLength > MAX_FRAME_LENGTH) {
                ctx.close(); // 消息长度异常，关闭连接
                return;
            }

            // 检查是否有足够的数据读取完整消息
            if (in.readableBytes() < msgLength) {
                in.resetReaderIndex(); // 数据不足，回退并等待
                return;
            }

            String msgStr = in.readCharSequence(msgLength, Charset.forName("UTF-8")).toString();
            Object msg;
            if (msgType == MsgTypeEnum.REQ.getCode()) {
                msg = JSON.parseObject(msgStr, NettyReq.class);
            } else if (msgType == MsgTypeEnum.RESP.getCode()) {
                msg = JSON.parseObject(msgStr, NettyResp.class);
            } else {
                msg = msgStr;
            }

            out.add(msg);
        } catch (Exception e) {
            // 解码异常，关闭连接
            ctx.close();
            throw e;
        }
    }
}
