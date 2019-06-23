package com.jsj.rpc.protocol;

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
    private final byte serializationType;

    /**
     * 协议体数据长度（4byte)
     */
    private int bodyLength;

    /**
     * 默认协议序号
     */
    public static byte PROTOCOL_CODE = (byte) 0xAC;

    public DefaultHeader(byte messageType, byte serializationType, int bodyLength) {
        this(PROTOCOL_CODE, messageType, serializationType, bodyLength);
    }

    public DefaultHeader(byte protocolCode, byte messageType, byte serializationType, int bodyLength) {
        this.protocolCode = protocolCode;
        this.messageType = messageType;
        this.serializationType = serializationType;
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
        return this.serializationType;
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
    public String toString() {
        return "DefaultHeader{" +
                "protocolCode=" + protocolCode +
                ", messageType=" + messageType +
                ", serializationType=" + serializationType +
                ", bodyLength=" + bodyLength +
                '}';
    }
}
