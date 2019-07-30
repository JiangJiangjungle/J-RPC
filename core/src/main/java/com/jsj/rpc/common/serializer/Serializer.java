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
     */
    <T> T deSerialize(byte[] bytes, Class<T> clazz) throws SerializationException;

    /**
     * 序列化
     */
    <T> byte[] serialize(T obj) throws SerializationException;
}
