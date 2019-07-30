package com.jsj.rpc.common.serializer;

import com.alibaba.fastjson.JSON;
import com.jsj.rpc.exception.SerializationException;

/**
 * @author jiangshenjie
 */
public class JSONSerializer implements Serializer {

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) throws SerializationException {
        return JSON.parseObject(bytes, clazz);
    }

    @Override
    public <T> byte[] serialize(T obj) throws SerializationException{
        //使用fastJSON进行序列化
        return JSON.toJSONBytes(obj);
    }


}
