package com.jsj.rpc.protocol;

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
    private final Body content;

    @Override
    public boolean emptyBody() {
        return content == null;
    }

    @Override
    public Header getHeader() {
        return this.header;
    }

    @Override
    public Body getBody() {
        return this.content;
    }

    public NettyMessage(Header header) {
        this(header, null);
    }

    public NettyMessage(Header header, Body content) {
        this.header = header;
        this.content = content;
    }

    @Override
    public String toString() {
        String msg = "NettyMessage{" +
                "header=" + header.toString();
        if (content != null) {
            msg += ", content=" + content.toString();
        }
        msg += '}';
        return msg;
    }
}
