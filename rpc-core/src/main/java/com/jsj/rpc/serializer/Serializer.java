package com.jsj.rpc.serializer;

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
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T deSerialize(byte[] bytes, Class<T> clazz) throws SerializeException;

    /**
     * 序列化
     *
     * @param obj
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> byte[] serialize(T obj) throws SerializeException;
}
