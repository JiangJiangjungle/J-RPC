package com.jsj.rpc.common;

/**
 * 自定义协议封装
 *
 * @author jsj
 * @date 2018-12-2
 */
public class NettyMessage {
    /**
     * 协议首部
     */
    private Header header;
    /**
     * 数据内容
     */
    private Body content;

    public NettyMessage(Header header) {
        this.header = header;
    }

    public NettyMessage(Header header, Body content) {
        this.header = header;
        this.content = content;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Body getContent() {
        return content;
    }

    public void setContent(Body content) {
        this.content = content;
    }
}
