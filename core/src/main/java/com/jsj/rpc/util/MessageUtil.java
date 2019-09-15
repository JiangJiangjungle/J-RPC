package com.jsj.rpc.util;

import com.jsj.rpc.RpcRequest;
import com.jsj.rpc.RpcResponse;
import com.jsj.rpc.common.message.*;
import com.jsj.rpc.common.serializer.SerializerTypeEnum;
import com.jsj.rpc.exception.SerializationException;
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
    public static Message createMessage(MessageTypeEnum messageType, SerializerTypeEnum serializerType, Body content) {
        Header header = new DefaultHeader(messageType.getValue(), serializerType.getValue());
        return createMessage(header, content);
    }

    /**
     * 创建一个心跳请求Message
     *
     * @return
     */
    public static Message createHeartBeatRequest() {
        return createMessage(MessageTypeEnum.HEART_BEAT_REQUEST, SerializerTypeEnum.DEFAULT, null);
    }

    /**
     * 创建一个心跳响应Message
     *
     * @return
     */
    public static Message createHeartBeatResponse() {
        return createMessage(MessageTypeEnum.HEART_BEAT_RESPONSE, SerializerTypeEnum.DEFAULT, null);
    }

    /**
     * 利用数据就绪的缓冲区，解析首部并封装一个Header对象返回
     *
     * @param in
     * @return
     */
    public static Header byteBufTransToHeader(ByteBuf in) {
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
    public static Body byteBufTransToBody(Header header, ByteBuf in) throws SerializationException {
        if (header.bodyLength() == 0) return null;
        byte[] data = new byte[header.bodyLength()];
        in.readBytes(data);
        if (MessageTypeEnum.RPC_REQUEST.getValue() == header.messageType()) {
            return SerializationUtil.deserialize(header.serializationType(), RpcRequest.class, data);
        } else {
            return SerializationUtil.deserialize(header.serializationType(), RpcResponse.class, data);
        }
    }
}
