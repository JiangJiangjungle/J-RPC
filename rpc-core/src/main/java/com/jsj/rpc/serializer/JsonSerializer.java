package com.jsj.rpc.serializer;

import com.alibaba.fastjson.JSON;

/**
 * @author jiangshenjie
 */
public class JsonSerializer implements Serializer {

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) throws SerializeException {
        try {
            return JSON.parseObject(bytes, clazz);
        } catch (Exception e) {
            throw new SerializeException();
        }
    }

    @Override
    public <T> byte[] serialize(T obj) throws SerializeException {
        try {
            //使用fastJSON进行序列化
            return JSON.toJSONBytes(obj);
        } catch (Exception e) {
            throw new SerializeException();
        }
    }

}
