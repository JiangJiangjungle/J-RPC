package com.jsj.rpc.codec.serializer;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.jsj.rpc.exception.SerializationException;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiangshenjie
 */
public class ProtostuffSerializer implements Serializer {

    private Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();
    private Objenesis objenesis = new ObjenesisStd(true);

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) throws SerializationException {
        try {
            T message = objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(bytes, message, schema);
            return message;
        } catch (Exception e) {
            throw new SerializationException(e.getMessage());
        }
    }

    @Override
    public <T> byte[] serialize(T obj) throws SerializationException {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new SerializationException(e.getMessage());
        } finally {
            buffer.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) this.cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            cachedSchema.put(cls, schema);
        }
        return schema;
    }
}
