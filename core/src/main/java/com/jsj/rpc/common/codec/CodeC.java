package com.jsj.rpc.common.codec;

import io.netty.channel.ChannelHandler;

public interface CodeC {

    /**
     * Create an encoder instance.
     *
     * @return new encoder instance
     */
    ChannelHandler newEncoder();

    /**
     * Create an decoder instance.
     *
     * @return new decoder instance
     */
    ChannelHandler newDecoder();
}
