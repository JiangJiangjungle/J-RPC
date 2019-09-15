package com.jsj.rpc.common.message;

import com.jsj.rpc.exception.SerializationException;
import com.jsj.rpc.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * Messag实现类
 *
 * @author jsj
 * @date 2019-04-04
 */
public class NettyMessage implements Message {
    /**
     * 协议首部
     */
    private final Header header;
    /**
     * 数据内容
     */
    private final Body body;

    public NettyMessage(Header header, Body body) {
        this.header = header;
        this.body = body;
    }

    @Override
    public Header getHeader() {
        return this.header;
    }

    @Override
    public boolean emptyBody() {
        return body == null;
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    public NettyMessage(Header header) {
        this(header, null);
    }

    @Override
    public ByteBuf getBytes() throws SerializationException {
        byte[] bodyBytes = null;
        //设置body长度信息
        if (header.bodyLength() == 0 && !emptyBody()) {
            bodyBytes = SerializationUtil.serialize(body, header.serializationType());
            header.setBodyLength(bodyBytes.length);
        }
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(Header.PROTOCOL_HEADER_BYTES + header.bodyLength());
        byteBuf.writeBytes(header.getBytes());
        if (bodyBytes != null) {
            byteBuf.writeBytes(bodyBytes);
        }
        return byteBuf;
    }

    @Override
    public String toString() {
        String msg = "NettyMessage{" +
                "header=" + header.toString();
        if (body != null) {
            msg += ", body=" + body.toString();
        }
        msg += '}';
        return msg;
    }
}
