package com.jsj.rpc.codec;

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
public class BaseEncoder extends MessageToByteEncoder<Object> {
    private final Protocol protocol;

    public BaseEncoder(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return super.acceptOutboundMessage(msg);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object content, ByteBuf out) throws Exception {
        ByteBuf byteBuf = null;
        try {
            byteBuf = protocol.encodeMsg(content);
            out.writeBytes(byteBuf);
            log.debug("Write message: {}", content.toString());
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
            }
        }
    }
}