package com.jsj.rpc.protocol;

/**
 * @author jiangshenjie
 */
public interface Packet {
    /**
     * 释放所有ByteBuf
     */
    void release();
}
