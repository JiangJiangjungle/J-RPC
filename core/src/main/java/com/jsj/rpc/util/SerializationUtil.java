package com.jsj.rpc.util;

import com.jsj.rpc.codec.serializer.Serializer;
import com.jsj.rpc.exception.SerializationException;
import com.jsj.rpc.protocol.Body;
import com.jsj.rpc.codec.serializer.SerializerTypeEnum;

/**
 * 序列化工具类（提供 Java原生、JSON 和 Protostuff 3种序列化/反序列化实现）
 *
 * @author jsj
 * @date 2018-11-4
 */
public class SerializationUtil {

    /**
     * 序列化（对象 -> 字节数组）
     */
    public static byte[] serialize(Body obj, byte type) throws SerializationException {
        SerializerTypeEnum serializerType = SerializerTypeEnum.get(type);
        Serializer serializer = serializerType.getSerializer();
        return serializer.serialize(obj);
    }

    /**
     * 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserialize(byte type, Class<T> cls, byte[] data) throws SerializationException {
        SerializerTypeEnum serializerType = SerializerTypeEnum.get(type);
        Serializer serializer = serializerType.getSerializer();
        return serializer.deSerialize(data, cls);
    }
}
