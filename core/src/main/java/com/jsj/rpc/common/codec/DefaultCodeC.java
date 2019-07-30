package com.jsj.rpc.common.codec;

import io.netty.channel.ChannelHandler;

/**
 * 编解码器
 *
 * @author jsj
 * @date 2018-11-6
 */
public class DefaultCodeC implements CodeC {
    private static DefaultCodeC codeC = new DefaultCodeC();

    private DefaultCodeC() {
    }

    public static DefaultCodeC getInstance() {
        return codeC;
    }

    @Override
    public ChannelHandler newEncoder() {
        return new BaseEncoder();
    }

    @Override
    public ChannelHandler newDecoder() {
        return new BaseDecoder();
    }
}
