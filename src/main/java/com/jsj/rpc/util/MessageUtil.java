package com.jsj.rpc.util;

import com.jsj.rpc.common.Body;
import com.jsj.rpc.common.Header;
import com.jsj.rpc.common.NettyMessage;

public class MessageUtil {

    public static NettyMessage buildNormalMessage(Header header, Body content) {
        return new NettyMessage(header, content);
    }

    public static Header buildHeartBeatRequest() {
        return new Header(0,Header.HEART_BEAT_REQUEST);
    }

    public static Header buildHeartBeatResponse() {
        return new Header(0,Header.HEART_BEAT_RESPONSE);
    }

}
