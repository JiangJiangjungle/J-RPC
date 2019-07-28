package com.jsj.rpc.codec;

import io.netty.channel.ChannelHandler;

public class DefaultServerCodeC implements CodeC {

    @Override
    public ChannelHandler newEncoder() {
        return new BaseEncoder();
    }

    @Override
    public ChannelHandler newDecoder() {
        return new BaseDecoder();
    }
}
