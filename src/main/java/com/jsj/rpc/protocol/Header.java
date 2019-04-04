package com.jsj.rpc.protocol;

public interface Header {
    /**
     * 协议首部字节长度
     */
    int PROTOCOL_HEADER_BYTES = 7;

    byte protocolCode();

    byte messageType();

    byte serializationType();

    int bodyLength();

    void setBodyLength(int bodyLength);
}
