package com.jsj.rpc.codec;

import com.jsj.rpc.protocol.SerializationTypeEnum;
import io.netty.channel.ChannelHandler;

/**
 * 编解码方案
 *
 * @author jsj
 * @date 2018-11-6
 */
public class DefaultCodeC implements CodeC {

    private final SerializationTypeEnum serializationType;

    public DefaultCodeC(SerializationTypeEnum serializationType) {
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
