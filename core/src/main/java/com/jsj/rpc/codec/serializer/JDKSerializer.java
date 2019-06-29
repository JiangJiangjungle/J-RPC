package com.jsj.rpc.codec.serializer;

import com.jsj.rpc.exception.SerializationException;

import java.io.*;

/**
 * @author jiangshenjie
 */
public class JDKSerializer implements Serializer {
    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) throws SerializationException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException(e.getMessage());
        }
    }

    @Override
    public <T> byte[] serialize(T obj) throws SerializationException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(obj);
            oos.flush();
            return os.toByteArray();
        } catch (IOException e) {
            throw new SerializationException(e.getMessage());
        }
    }
}
