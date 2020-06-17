package com.jsj.rpc.codec;


import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.exception.NotEnoughDataException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * RPC 解码器
 *
 * @author jsj
 * @date 2018-11-6
 */
@Slf4j
public class BaseDecoder extends ByteToMessageDecoder {
    private final Protocol protocol;

    public BaseDecoder(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            out.add(protocol.parseHeaderAndPackage(in));
        } catch (NotEnoughDataException e) {
        }
    }
}
