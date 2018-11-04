package com.jsj.nettyrpc.util;

import com.alibaba.fastjson.JSON;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工具类（提供 JSON 和 Protostuff 2种实现）
 *
 * @author jsj
 * @date 2018-11-4
 */
public class SerializationUtil {

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private static Objenesis objenesis = new ObjenesisStd(true);

    private SerializationUtil() {
    }

    /**
     * 基于 Protostuff 序列化（对象 -> 字节数组）
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serializeByProtostuff(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 基于 Protostuff 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserializeByProtostuff(byte[] data, Class<T> cls) {
        try {
            T message = objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * 基于 JSON 序列化（对象 -> 字节数组）
     */
    public static <T> byte[] serializeByJSON(T obj) {
        //使用fastJSON进行序列化
        return JSON.toJSONBytes(obj);
    }

    /**
     * 基于 JSON 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserializeByJSON(byte[] data, Class<T> cls) {
        return JSON.parseObject(data, cls);
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            cachedSchema.put(cls, schema);
        }
        return schema;
    }
}
