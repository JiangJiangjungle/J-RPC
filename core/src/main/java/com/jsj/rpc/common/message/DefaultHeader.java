package com.jsj.rpc.common.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * 自定义协议首部
 *
 * @author jsj
 * @date 2018-12-2
 */
public class DefaultHeader implements Header {

    /**
     * 协议序号（1byte）
     */
    private final byte protocolCode;

    /**
     * 消息类型（1byte）
     */
    private final byte messageType;

    /**
     * 序列化类型（1byte）
     */
    private final byte serializerType;

    /**
     * 协议体数据长度（4byte)
     */
    private int bodyLength;

    /**
     * 默认协议序号
     */
    public static byte PROTOCOL_CODE = (byte) 0xAC;

    public DefaultHeader(byte messageType, byte serializerType) {
        this(PROTOCOL_CODE, messageType, serializerType, 0);
    }

    public DefaultHeader(byte protocolCode, byte messageType, byte serializerType, int bodyLength) {
        this.protocolCode = protocolCode;
        this.messageType = messageType;
        this.serializerType = serializerType;
        this.bodyLength = bodyLength;
    }

    @Override
    public byte protocolCode() {
        return this.protocolCode;
    }

    @Override
    public byte messageType() {
        return this.messageType;
    }

    @Override
    public byte serializationType() {
        return this.serializerType;
    }

    @Override
    public int bodyLength() {
        return this.bodyLength;
    }

    @Override
    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    @Override
    public ByteBuf getBytes() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(Header.PROTOCOL_HEADER_BYTES);
        byteBuf.writeByte(protocolCode).writeByte(messageType).writeByte(serializerType).writeInt(bodyLength);
        return byteBuf;
    }

    @Override
    public String toString() {
        return "DefaultHeader{" +
                "protocolCode=" + protocolCode +
                ", messageType=" + messageType +
                ", serializerType=" + serializerType +
                ", bodyLength=" + bodyLength +
                '}';
    }
}
