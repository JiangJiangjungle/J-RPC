package com.jsj.nettyrpc.codec;

import io.netty.channel.ChannelHandler;

/**
 * 编解码方案
 *
 * @author jsj
 * @date 2018-11-6
 */
public class DefaultCodeC implements CodeC {
    private final Class<?> encoderClass;
    private final Class<?> decoderClass;

    private final CodeStrategy strategy;

    public DefaultCodeC(Class<?> encoderClass, Class<?> decoderClass) {
        this(encoderClass, decoderClass, CodeStrategy.DEAULT);
    }

    public DefaultCodeC(Class<?> encoderClass, Class<?> decoderClass, CodeStrategy strategy) {
        this.encoderClass = encoderClass;
        this.decoderClass = decoderClass;
        this.strategy = strategy;
    }

    @Override
    public ChannelHandler newEncoder() {
        return new RpcEncoder(this.encoderClass, this.strategy);
    }

    @Override
    public ChannelHandler newDecoder() {
        return new RpcDecoder(this.decoderClass, this.strategy);
    }
}
