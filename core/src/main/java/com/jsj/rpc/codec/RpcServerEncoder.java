//package com.jsj.rpc.codec;
//
//import com.jsj.rpc.codec.serializer.SerializerTypeEnum;
//import com.jsj.rpc.exception.CodecException;
//import com.jsj.rpc.protocol.Body;
//import com.jsj.rpc.protocol.Message;
//import com.jsj.rpc.protocol.MessageTypeEnum;
//import com.jsj.rpc.protocol.RpcRequest;
//import com.jsj.rpc.util.MessageUtil;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.handler.codec.MessageToByteEncoder;
//
//public class RpcServerEncoder implements MessageToByteEncoder<Message> {
//
//    private final SerializerTypeEnum serializationType;
//
//    public BaseEncoder(SerializerTypeEnum serializationType) {
//        this.serializationType = serializationType;
//    }
//
//    @Override
//    protected void encode(ChannelHandlerContext channelHandlerContext, Message o, ByteBuf byteBuf) throws Exception {
//        if (in instanceof Message) {
//            MessageUtil.messageTransToByteBuf((Message) in, out);
//        } else if (in instanceof Body) {
//            MessageTypeEnum messageTypeEnum = in instanceof RpcRequest ? MessageTypeEnum.RPC_REQUEST : MessageTypeEnum.RPC_RESPONSE;
//            Message msg = MessageUtil.createMessage(messageTypeEnum, serializationType, (Body) in);
//            MessageUtil.messageTransToByteBuf(msg, out);
//        } else {
//            throw new CodecException("传入非法的对象类型,无法进行编码！");
//        }
//    }
//
//    @Override
//    public <I> void encode(ChannelHandlerContext ctx, I in, ByteBuf out) throws Exception {
//        if (in instanceof Message) {
//            MessageUtil.messageTransToByteBuf((Message) in, out);
//        } else if (in instanceof Body) {
//            MessageTypeEnum messageTypeEnum = in instanceof RpcRequest ? MessageTypeEnum.RPC_REQUEST : MessageTypeEnum.RPC_RESPONSE;
//            Message msg = MessageUtil.createMessage(messageTypeEnum, serializationType, (Body) in);
//            MessageUtil.messageTransToByteBuf(msg, out);
//        } else {
//            throw new CodecException("传入非法的对象类型,无法进行编码！");
//        }
//    }
//}
