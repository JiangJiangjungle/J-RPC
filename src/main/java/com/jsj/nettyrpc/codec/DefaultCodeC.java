package com.jsj.nettyrpc.codec;

import io.netty.channel.ChannelHandler;

public class DefaultCodeC implements CodeC {
    private Class<?> genericClass;

    public DefaultCodeC(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public ChannelHandler newEncoder() {
        return new RpcEncoder(this.genericClass);
    }

    @Override
    public ChannelHandler newDecoder() {
        return new RpcDecoder(this.genericClass);
    }
}
