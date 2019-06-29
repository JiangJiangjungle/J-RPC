package com.jsj.rpc.codec;


import com.jsj.rpc.codec.serializer.SerializerTypeEnum;
import com.jsj.rpc.protocol.*;
import com.jsj.rpc.exception.CodecException;
import com.jsj.rpc.util.MessageUtil;
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

    private final SerializerTypeEnum serializationType;

    public RpcEncoder(SerializerTypeEnum serializationType) {
        this.serializationType = serializationType;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (in instanceof Message) {
            MessageUtil.messageTransToByteBuf((Message) in, out);
        } else if (in instanceof Body) {
            MessageTypeEnum messageTypeEnum = in instanceof RpcRequest ? MessageTypeEnum.RPC_REQUEST : MessageTypeEnum.RPC_RESPONSE;
            Message msg = MessageUtil.createMessage(messageTypeEnum, serializationType, (Body) in);
            MessageUtil.messageTransToByteBuf(msg, out);
        } else {
            throw new CodecException("传入非法的对象类型,无法进行编码！");
        }
    }
}