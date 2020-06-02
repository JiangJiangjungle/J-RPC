package com.jsj.rpc.protocol;

import io.netty.buffer.ByteBuf;

/**
 * @author jiangshenjie
 */
public interface Packet {
    /**
     * 释放所有ByteBuf
     */
    void release();

    /**
     * 获取消息实体对应的ByteBuf
     *
     * @return
     */
    ByteBuf getBody();
}
