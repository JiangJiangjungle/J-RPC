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

    private final Class<?> genericClass;

    private final CodeStrategy strategy;

    public static int DEFAULT_LENGTH_FIELD_OFFSET = 4;

    public RpcDecoder(Class<?> genericClass, CodeStrategy strategy) {
        this.genericClass = genericClass;
        this.strategy = strategy;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < DEFAULT_LENGTH_FIELD_OFFSET) {
            throw new Exception("消息格式错误！");
        }
        //标记当前的readIndex的位置
        in.markReaderIndex();
        int dataLength = in.readInt();
        //消息体长度如果小于传送过来的消息长度，则resetReaderIndex，把readIndex重置到mark的地方
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        out.add(SerializationUtil.deserialize(data, genericClass, strategy));
    }
}
