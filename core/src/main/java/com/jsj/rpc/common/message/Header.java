package com.jsj.rpc.common.message;

import io.netty.buffer.ByteBuf;

/**
 * @author jiangshenjie
 */
public interface Header {
    /**
     * 协议首部字节长度
     */
    int PROTOCOL_HEADER_BYTES = 7;

    /**
     * 获取协议版本号
     *
     * @return
     */
    byte protocolCode();

    /**
     * 获取消息类型
     *
     * @return
     */
    byte messageType();

    /**
     * 获取序列化类型
     *
     * @return
     */
    byte serializationType();

    /**
     * 获取body长度
     *
     * @return
     */
    int bodyLength();

    /**
     * 转化为ByteBuf前设置body长度
     *
     * @param bodyLength
     */
    void setBodyLength(int bodyLength);

    /**
     * 转化为ByteBuf
     *
     * @return
     */
    ByteBuf getBytes();
}
