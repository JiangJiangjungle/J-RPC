package com.jsj.rpc.codec;

import com.jsj.rpc.codec.serializer.SerializerTypeEnum;
import io.netty.channel.ChannelHandler;

/**
 * 编解码方案
 *
 * @author jsj
 * @date 2018-11-6
 */
public class DefaultCodeC implements CodeC {

    private final SerializerTypeEnum serializationType;

    public DefaultCodeC(SerializerTypeEnum serializationType) {
        this.serializationType = serializationType;
    }

    @Override
    public ChannelHandler newEncoder() {
        return new RpcEncoder(serializationType);
    }

    @Override
    public ChannelHandler newDecoder() {
        return new RpcDecoder();
    }
}
