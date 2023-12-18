package com.xihua.socketm.common.handler;

import com.alibaba.fastjson.JSON;
import com.xihua.socketm.common.domain.NettyReq;
import com.xihua.socketm.common.domain.NettyResp;
import com.xihua.socketm.common.enums.MsgTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;
import java.util.List;

public class ByteBufDecoder extends ByteToMessageDecoder {

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte msgType = in.readByte();
        int msgLength = in.readInt();
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
    }
}
