package com.xihua.nettym.common.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.xihua.nettym.common.domain.NettyResp;
import com.xihua.nettym.common.enums.MsgTypeEnum;
import com.xihua.nettym.common.domain.NettyReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteBufEncoder extends MessageToByteEncoder<Object> {
    private static final Logger logger = LoggerFactory.getLogger(ByteBufEncoder.class);
    private static final int MAX_MESSAGE_LENGTH = 1024 * 1024; // 1MB 最大消息长度

    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        MsgTypeEnum msgType;
        if (msg instanceof NettyReq) {
            msgType = MsgTypeEnum.REQ;
        } else if (msg instanceof NettyResp) {
            msgType = MsgTypeEnum.RESP;
        } else {
            msgType = MsgTypeEnum.DEFAULT;
        }

        byte[] msgBytes;
        try {
            // 对于字符串类型，直接使用字符串本身
            if (msg instanceof String) {
                msgBytes = ((String) msg).getBytes("UTF-8");
            } else {
                msgBytes = JSON.toJSONString(msg).getBytes("UTF-8");
            }
        } catch (JSONException e) {
            logger.error("Failed to serialize message: {}", msg, e);
            throw new Exception("Message serialization failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during message encoding: {}", msg, e);
            throw new Exception("Message encoding failed: " + e.getMessage(), e);
        }

        // 检查消息长度
        if (msgBytes.length > MAX_MESSAGE_LENGTH) {
            logger.error("Message too large: {} bytes, max: {} bytes", msgBytes.length, MAX_MESSAGE_LENGTH);
            throw new Exception("Message too large: " + msgBytes.length + " bytes, max: " + MAX_MESSAGE_LENGTH + " bytes");
        }

        out.writeByte(msgType.getCode());
        out.writeInt(msgBytes.length);
        out.writeBytes(msgBytes);
    }
}
