package com.xihua.socketm.common.enums;

public enum MsgTypeEnum {
    REQ((byte) 1),
    RESP((byte) 2),
    DEFAULT((byte) 3),
    ;

    private final byte code;

    MsgTypeEnum(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }
}
