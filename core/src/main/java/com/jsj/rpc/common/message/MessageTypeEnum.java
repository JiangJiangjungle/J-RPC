package com.jsj.rpc.common.message;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息类型
 *
 * @author jsj
 * @date 2019-04-04
 */
public enum MessageTypeEnum {
    /**
     * 心跳请求
     */
    HEART_BEAT_REQUEST("heartBeatRequest", (byte) 1),
    /**
     * 心跳响应
     */
    HEART_BEAT_RESPONSE("heartBeatResponse", (byte) 2),
    /**
     * rpc请求
     */
    RPC_REQUEST("rpcRequest", (byte) 4),
    /**
     * rpc响应
     */
    RPC_RESPONSE("rpcResponse", (byte) 8);

    static final Map<Byte, MessageTypeEnum> LOOK_UP = new HashMap<>();


    private String name;
    private byte value;

    MessageTypeEnum(String name, byte value) {
        this.name = name;
        this.value = value;
    }

    public static MessageTypeEnum get(byte value) {
        return LOOK_UP.get(value);
    }

    public String getName() {
        return name;
    }

    public byte getValue() {
        return value;
    }

    static {
        MessageTypeEnum messageTypeEnum;
        for (Object m : EnumSet.allOf(MessageTypeEnum.class)) {
            messageTypeEnum = (MessageTypeEnum) m;
            LOOK_UP.put(messageTypeEnum.value, messageTypeEnum);
        }
    }
}
