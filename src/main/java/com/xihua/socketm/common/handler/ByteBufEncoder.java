package com.xihua.socketm.common.handler;

import com.alibaba.fastjson.JSON;
import com.xihua.socketm.common.domain.NettyResp;
import com.xihua.socketm.common.enums.MsgTypeEnum;
import com.xihua.socketm.common.domain.NettyReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ByteBufEncoder extends MessageToByteEncoder<Object> {

    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        MsgTypeEnum msgType;
        if (msg instanceof NettyReq) {
            msgType = MsgTypeEnum.REQ;
        } else if (msg instanceof NettyResp) {
            msgType = MsgTypeEnum.RESP;
        } else {
            msgType = MsgTypeEnum.DEFAULT;
        }

        byte[] msgBytes = JSON.toJSONString(msg).getBytes("UTF-8");
        out.writeByte(msgType.getCode());
        out.writeInt(msgBytes.length);
        out.writeBytes(msgBytes);
    }
}
