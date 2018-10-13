package com.jsj.nettyrpc.codec;

import io.netty.channel.ChannelHandler;

public class DefaultCodeC implements CodeC {
    private Class<?> encodeClass;
    private Class<?> decodeClass;

    public DefaultCodeC(Class<?> encodeClass, Class<?> decodeClass) {
        this.encodeClass = encodeClass;
        this.decodeClass = decodeClass;
    }

    @Override
    public ChannelHandler newEncoder() {
        return new RpcEncoder(this.encodeClass);
    }

    @Override
    public ChannelHandler newDecoder() {
        return new RpcDecoder(this.decodeClass);
    }
}
