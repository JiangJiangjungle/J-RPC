package com.jsj.rpc.codec;

import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.RpcPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC 编码器
 *
 * @author jsj
 * @date 2018-11-6
 */
@Slf4j
public class BaseEncoder extends MessageToByteEncoder<RpcPacket> {
    private final Protocol protocol;

    public BaseEncoder(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcPacket packet, ByteBuf out) throws Exception {
        ByteBuf byteBuf = null;
        try {
            byteBuf = protocol.encodePacket(packet);
            out.writeBytes(byteBuf);
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
            }
        }
    }
}