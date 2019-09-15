package com.jsj.rpc.common.message;

import com.jsj.rpc.exception.SerializationException;
import io.netty.buffer.ByteBuf;

/**
 * RPC 消息封装
 *
 * @author jiangshenjie
 */
public interface Message {
    /**
     * 消息body是否为空
     *
     * @return
     */
    boolean emptyBody();

    /**
     * 获取消息首部
     *
     * @return
     */
    Header getHeader();

    /**
     * 获取消息body
     *
     * @return
     */
    Body getBody();

    /**
     * 转化为ByteBuf
     *
     * @return
     * @throws SerializationException
     */
    ByteBuf getBytes() throws SerializationException;
}
