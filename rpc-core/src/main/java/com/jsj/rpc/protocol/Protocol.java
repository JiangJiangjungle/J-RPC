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
     * 将RpcPacket封装成协议报文
     *
     * @param packet 待发送的消息
     * @return ByteBuf
     * @throws Exception
     */
    ByteBuf encodePacket(RpcPacket packet) throws Exception;

    /**
     * 解析报文的header，将body封装成RpcPacket对象
     *
     * @param in
     * @return
     * @throws BadSchemaException
     * @throws NotEnoughDataException
     */
    RpcPacket parseHeader(ByteBuf in) throws BadSchemaException, NotEnoughDataException;

    /**
     * Rpc Server: 从packet中反序列化出RpcRequest对象
     *
     * @param packet
     * @return
     * @throws Exception
     */
    RpcRequest decodeRequest(RpcPacket packet) throws Exception;

    /**************** 仅Rpc Client需要实现的函数 *******************/


    /**
     * Rpc Client: 从RpcPacket反序列化出RpcResponse对象
     *
     * @param packet
     * @return
     * @throws Exception
     */
    RpcResponse decodeResponse(RpcPacket packet, ChannelInfo channelInfo) throws Exception;
}
