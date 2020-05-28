package com.jsj.rpc.serializer;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author jiangshenjie
 */
public class JsonSerializer implements Serializer {

    @Override
    public <T> T deSerialize(ByteBuf byteBuf, Class<T> clazz) throws SerializeException {
        try {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            return JSON.parseObject(bytes, clazz);
        } catch (Exception e) {
            throw new SerializeException();
        }
    }

    @Override
    public <T> ByteBuf serialize(T obj) throws SerializeException {
        try {
            //使用fastJSON进行序列化
            byte[] jsonBytes = JSON.toJSONBytes(obj);
            //写入ByteBuf
            ByteBuf byteBuf = Unpooled.buffer(jsonBytes.length);
            byteBuf.writeBytes(jsonBytes);
            return byteBuf;
        } catch (Exception e) {
            throw new SerializeException();
        }
    }

}
