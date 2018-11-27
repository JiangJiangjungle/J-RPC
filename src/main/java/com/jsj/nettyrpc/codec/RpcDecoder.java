package com.jsj.nettyrpc.codec;


import com.jsj.nettyrpc.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * RPC 解码器
 *
 * @author jsj
 * @date 2018-11-6
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    private CodeStrategy strategy;

    public static int DEFAULT_LENGTH_FIELD_OFFSET=4;

    public RpcDecoder(Class<?> genericClass, CodeStrategy strategy) {
        this.genericClass = genericClass;
        this.strategy = strategy;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < DEFAULT_LENGTH_FIELD_OFFSET) {
            throw new Exception("消息格式错误！");
        }
//        in.markReaderIndex();
        int dataLength = in.readInt();
//        if (in.readableBytes() < dataLength) {
//            in.resetReaderIndex();
//            return;
//        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        if (CodeStrategy.JDK == strategy) {
            out.add(SerializationUtil.deserializeWithJDK(data));
        } else if (CodeStrategy.JSON == strategy) {
            out.add(SerializationUtil.deserializeWithJSON(data, genericClass));
        } else {
            out.add(SerializationUtil.deserializeWithProtostuff(data, genericClass));
        }
    }
}
