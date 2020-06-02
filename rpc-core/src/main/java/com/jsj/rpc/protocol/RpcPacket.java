package com.jsj.rpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * @author jiangshenjie
 */
public class RpcPacket implements Packet {
    ByteBuf body;
    Channel channel;

    public RpcPacket() {
    }

    public RpcPacket(ByteBuf body) {
        this.body = body;
    }

    @Override
    public void release() {
        this.body.release();
    }

    public ByteBuf getBody() {
        return body;
    }

    public void setBody(ByteBuf body) {
        this.body = body;
    }
}
