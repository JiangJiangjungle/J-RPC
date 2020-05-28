package com.jsj.rpc.serializer;

import io.netty.buffer.ByteBuf;

/**
 * 序列化工具
 *
 * @author jiangshenjie
 * @date 2019-6-29
 */
public interface Serializer {

    /**
     * 反序列化
     *
     * @param byteBuf
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T deSerialize(ByteBuf byteBuf, Class<T> clazz) throws SerializeException;

    /**
     * 序列化
     *
     * @param obj
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> ByteBuf serialize(T obj) throws SerializeException;
}
