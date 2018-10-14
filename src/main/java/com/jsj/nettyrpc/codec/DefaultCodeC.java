package com.jsj.nettyrpc.codec;

import io.netty.channel.ChannelHandler;

public class DefaultCodeC implements CodeC {
    private Class<?> encoderClass;
    private Class<?> decoderClass;

    public DefaultCodeC(Class<?> encoderClass, Class<?> decoderClass) {
        this.encoderClass = encoderClass;
        this.decoderClass = decoderClass;
    }

    @Override
    public ChannelHandler newEncoder() {
        return new RpcEncoder(this.encoderClass);
    }

    @Override
    public ChannelHandler newDecoder() {
        return new RpcDecoder(this.decoderClass);
    }
}
