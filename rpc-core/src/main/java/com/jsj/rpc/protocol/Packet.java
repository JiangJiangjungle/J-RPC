package com.jsj.rpc.protocol;

import io.netty.buffer.ByteBuf;

/**
 * @author jiangshenjie
 */
public class Packet {
    ByteBuf body;

    public Packet() {
    }

    public Packet(ByteBuf body) {
        this.body = body;
    }

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
