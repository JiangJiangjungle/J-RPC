package com.jsj.rpc.protocol.standard;

import com.jsj.rpc.protocol.Packet;
import io.netty.buffer.ByteBuf;

/**
 * @author jiangshenjie
 */
public class RpcPacket implements Packet {
    ByteBuf body;

    public RpcPacket() {
    }

    public RpcPacket(ByteBuf body) {
        this.body = body;
    }

    @Override
    public void release() {
        this.body.release();
    }

    @Override
    public ByteBuf getBody() {
        return body;
    }

    public void setBody(ByteBuf body) {
        this.body = body;
    }
}
