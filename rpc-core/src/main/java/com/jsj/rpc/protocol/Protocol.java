package com.jsj.rpc.protocol;

import com.jsj.rpc.protocol.exception.BadSchemaException;
import com.jsj.rpc.protocol.exception.NotEnoughDataException;
import com.jsj.rpc.serializer.SerializeException;
import io.netty.buffer.ByteBuf;

/**
 * @author jiangshenjie
 */
public interface Protocol {
    /**
     * 序列化message对象
     *
     * @param message 待发送的消息
     * @return ByteBuf
     * @throws Exception
     */
    ByteBuf encodeMsg(Object message) throws SerializeException;

    /**
     * 解析header，将body封装成RpcPacket对象
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
     * @throws SerializeException
     */
    RpcRequest decodeRequest(RpcPacket packet) throws Exception;

    /**************** 仅Rpc Client需要实现的函数 *******************/


    /**
     * Rpc Client: 从ByteBuf反序列化出RpcResponse对象
     *
     * @param packet
     * @return
     * @throws SerializeException
     */
    RpcResponse decodeResponse(RpcPacket packet) throws SerializeException;
}
