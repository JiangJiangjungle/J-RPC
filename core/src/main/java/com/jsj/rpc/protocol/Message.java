package com.jsj.rpc.protocol;

/**
 * RPC 消息封装
 *
 * @author jiangshenjie
 */
public interface Message {

    boolean emptyBody();

    Header getHeader();

    Body getBody();
}
