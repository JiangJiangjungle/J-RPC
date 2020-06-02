package com.jsj.rpc.protocol;

import com.jsj.rpc.ChannelInfo;
import com.jsj.rpc.protocol.exception.BadSchemaException;
import com.jsj.rpc.protocol.exception.NotEnoughDataException;
import io.netty.buffer.ByteBuf;

/**
 * @author jiangshenjie
 */
public interface Protocol {
    /**
     * 将Packet封装成协议报文
     *
     * @param packet 待发送的消息
     * @return ByteBuf
     * @throws Exception
     */
    ByteBuf encodePacket(Packet packet) throws Exception;

    /**
     * 解析报文的header，将消息实体封装成Packet对象
     *
     * @param in
     * @return
     * @throws BadSchemaException
     * @throws NotEnoughDataException
     */
    Packet parseHeader(ByteBuf in) throws BadSchemaException, NotEnoughDataException;

    /**************** 仅Rpc Server需要实现的函数 *******************/

    /**
     * 从packet中获取RpcRequest对象
     *
     * @param packet
     * @return
     * @throws Exception
     */
    Request decodeAsRequest(Packet packet) throws Exception;

    /**************** 仅Rpc Client需要实现的函数 *******************/

    /**
     * 从Packet反序列化出RpcResponse对象
     *
     * @param packet
     * @param channelInfo
     * @return
     * @throws Exception
     */
    Response decodeAsResponse(Packet packet, ChannelInfo channelInfo) throws Exception;
}
