package com.jsj.rpc.protocol;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author jiangshenjie
 */
@Getter
@Setter
@NoArgsConstructor
public class RpcPacket {
    ByteBuf body;

    public RpcPacket(ByteBuf body) {
        this.body = body;
    }

    public void release() {
        this.body.release();
    }

    public byte[] getBytes(){
        ByteBuf bodyBuf = getBody();
        byte[] bodyBytes = new byte[bodyBuf.readableBytes()];
        bodyBuf.readBytes(bodyBytes);
        return bodyBytes;
    }
}
