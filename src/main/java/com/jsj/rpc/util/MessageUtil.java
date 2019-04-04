package com.jsj.rpc.util;

import com.jsj.rpc.protocol.RpcRequest;
import com.jsj.rpc.protocol.RpcResponse;
import com.jsj.rpc.protocol.*;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * 负责字节到消息封装对象之间的转换
 *
 * @author jsj
 * @date 2019-04-04
 */
public class MessageUtil {

    /**
     * 创建一个Message
     *
     * @return
     */
    public static Message createMessage(Header header, Body content) {
        return new NettyMessage(header, content);
    }

    /**
     * 根据Body对象，消息类型和序列化类型，创建一个Message
     *
     * @return
     */
    public static Message createMessage(MessageTypeEnum messageType, SerializationTypeEnum serializationType, Body content) {
        Header header = new DefaultHeader(messageType.getValue(), serializationType.getValue(), 0);
        return createMessage(header, content);
    }

    /**
     * 创建一个心跳请求Message
     *
     * @return
     */
    public static Message createHeartBeatRequestMessage() {
        return createMessage(MessageTypeEnum.HEART_BEAT_REQUEST, SerializationTypeEnum.NONE, null);
    }

    /**
     * 创建一个心跳响应Message
     *
     * @return
     */
    public static Message createHeartBeatResponseMessage() {
        return createMessage(MessageTypeEnum.HEART_BEAT_RESPONSE, SerializationTypeEnum.NONE, null);
    }

    /**
     * 利用数据就绪的缓冲区，解析首部并封装一个Header对象返回
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static Header byteBufTransToHeader(ByteBuf in) throws IOException {
        byte protocolCode = in.readByte();
        byte messageType = in.readByte();
        byte serializationType = in.readByte();
        int bodyLength = in.readInt();
        return new DefaultHeader(protocolCode, messageType, serializationType, bodyLength);
    }

    /**
     * 利用Header对象和数据就绪的缓冲区，解析并返回一个Body对象
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static Body byteBufTransToBody(Header header, ByteBuf in) throws ClassNotFoundException, IOException {
        if (header.bodyLength() == 0) return null;
        byte[] data = new byte[header.bodyLength()];
        in.readBytes(data);
        if (MessageTypeEnum.RPC_REQUEST.getValue() == header.messageType()) {
            return SerializationUtil.deserialize(header.serializationType(), RpcRequest.class, data);
        } else {
            return SerializationUtil.deserialize(header.serializationType(), RpcResponse.class, data);
        }
    }

    /**
     * Message对象写入缓冲区
     *
     * @param msg
     * @param out
     */
    public static void messageTransToByteBuf(Message msg, ByteBuf out) throws IOException {
        Header header = msg.getHeader();
        int bodyLength = 0;
        byte[] data = null;
        if (!msg.emptyBody() && SerializationTypeEnum.NONE.getValue() != header.serializationType()) {
            data = SerializationUtil.serialize(msg.getBody(), header.serializationType());
            bodyLength = data.length;
        }
        header.setBodyLength(bodyLength);
        headerTransToByteBuf(header, out);
        bodyTransToByteBuf(data, out);
    }

    /**
     * Header对象转换成字节写入缓冲区
     *
     * @param header
     * @param out
     */
    public static void headerTransToByteBuf(Header header, ByteBuf out) {
        out.writeByte(header.protocolCode());
        out.writeByte(header.messageType());
        out.writeByte(header.serializationType());
        out.writeInt(header.bodyLength());
    }

    /**
     * Body对象序列化后的字节写入缓冲区
     *
     * @param data
     * @param out
     */
    private static void bodyTransToByteBuf(byte[] data, ByteBuf out) {
        if (data != null) {
            out.writeBytes(data);
        }
    }
}
