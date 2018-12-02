package com.jsj.rpc.codec;

import io.netty.channel.ChannelHandler;

/**
 * 编解码方案
 *
 * @author jsj
 * @date 2018-11-6
 */
public class DefaultCodeC implements CodeC {

    private final CodeStrategy strategy;

    public DefaultCodeC(CodeStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public ChannelHandler newEncoder() {
        return new RpcEncoder(this.strategy);
    }

    @Override
    public ChannelHandler newDecoder() {
        return new RpcDecoder(this.strategy);
    }
}
