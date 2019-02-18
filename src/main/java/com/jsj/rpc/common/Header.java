package com.jsj.rpc.common;

/**
 * 自定义协议首部
 *
 * @author jsj
 * @date 2018-12-2
 */
public class Header {
    /**
     * 数据长度（不包含首部）
     */
    private int dataLength;
    /**
     * 心跳连接标志 1:心跳请求 2:心跳响应 4:rpc请求 8:rpc响应
     */
    private byte type = 0;

    public static byte HEART_BEAT_REQUEST = 1;
    public static byte HEART_BEAT_RESPONSE = 2;
    public static byte RPC_REQUEST = 4;
    public static byte RPC_RESPONSE = 8;

    public Header(int dataLength) {
        this.dataLength = dataLength;
    }

    public Header(int dataLength, byte type) {
        this.dataLength = dataLength;
        this.type = type;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }
}
