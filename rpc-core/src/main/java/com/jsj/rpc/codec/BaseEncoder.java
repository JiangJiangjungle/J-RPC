package com.jsj.rpc.codec;

import com.jsj.rpc.protocol.Packet;
import com.jsj.rpc.protocol.Protocol;
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
public class BaseEncoder extends MessageToByteEncoder<Packet> {
    private final Protocol protocol;

    public BaseEncoder(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        ByteBuf byteBuf = protocol.encodePacket(packet);
        try {
            out.writeBytes(byteBuf);
        } finally {
            byteBuf.release();
        }
    }
}