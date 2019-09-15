package com.jsj.rpc.common.serializer;

import com.jsj.rpc.exception.SerializationException;

/**
 * 序列化工具
 *
 * @author jiangshenjie
 * @date 2019-6-29
 */
public interface Serializer {

    /**
     * 反序列化
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     * @throws SerializationException
     */
    <T> T deSerialize(byte[] bytes, Class<T> clazz) throws SerializationException;

    /**
     * 序列化
     * @param obj
     * @param <T>
     * @return
     * @throws SerializationException
     */
    <T> byte[] serialize(T obj) throws SerializationException;
}
