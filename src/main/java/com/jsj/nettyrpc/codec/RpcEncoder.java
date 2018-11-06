package com.jsj.nettyrpc.codec;


import com.jsj.nettyrpc.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC 编码器
 *
 * @author jsj
 * @date 2018-11-6
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    private CodeStrategy strategy;

    public RpcEncoder(Class<?> genericClass, CodeStrategy strategy) {
        this.genericClass = genericClass;
        this.strategy = strategy;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            byte[] data;
            if (CodeStrategy.JDK == strategy) {
                data = SerializationUtil.serializeWithJDK(in);
            } else if (CodeStrategy.JSON == strategy) {
                data = SerializationUtil.serializeWithJSON(in);
            } else {
                data = SerializationUtil.serializeWithProtostuff(in);
            }
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}