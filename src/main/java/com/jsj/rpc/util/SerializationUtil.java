package com.jsj.rpc.util;

import com.alibaba.fastjson.JSON;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.jsj.rpc.codec.CodeStrategy;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工具类（提供 Java原生、JSON 和 Protostuff 3种序列化/反序列化实现）
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
    public static <T> byte[] serializeWithProtostuff(T obj) {
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
     * 基于 JSON 序列化（对象 -> 字节数组）
     */
    public static <T> byte[] serializeWithJSON(T obj) {
        //使用fastJSON进行序列化
        return JSON.toJSONBytes(obj);
    }

    /**
     * 基于 原生 序列化（对象 -> 字节数组）
     */
    public static <T> byte[] serializeWithJDK(T obj) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(obj);
            oos.flush();
            return os.toByteArray();
        }
    }

    /**
     * 基于 JSON 序列化（对象 -> 字节数组）
     */
    public static <T> byte[] serialize(T obj, CodeStrategy codeStrategy) throws IOException {
        if (obj == null) return null;
        if (CodeStrategy.JDK == codeStrategy) {
            return SerializationUtil.serializeWithJDK(obj);
        } else if (CodeStrategy.JSON == codeStrategy) {
            return SerializationUtil.serializeWithJSON(obj);
        } else {
            return SerializationUtil.serializeWithProtostuff(obj);
        }
    }

    /**
     * 基于 Protostuff 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserializeWithProtostuff(byte[] data, Class<T> cls) {
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
     * 基于 JSON 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserializeWithJSON(byte[] data, Class<T> cls) {
        return JSON.parseObject(data, cls);
    }

    /**
     * 基于 原生 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserializeWithJDK(byte[] data) throws ClassNotFoundException, IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data); ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        }
    }

    /**
     * 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserialize(byte[] data, Class<T> cls, CodeStrategy codeStrategy) throws ClassNotFoundException, IOException {
        if (data == null) {
            return null;
        } else if (CodeStrategy.JDK == codeStrategy) {
            T t = SerializationUtil.deserializeWithJDK(data);
            System.out.println(t.toString());
            return t;
        } else if (CodeStrategy.JSON == codeStrategy) {
            return SerializationUtil.deserializeWithJSON(data, cls);
        } else {
            return SerializationUtil.deserializeWithProtostuff(data, cls);
        }
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
