package com.jsj.rpc.protocol;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 序列化类型
 *
 * @author jsj
 * @date 2019-04-04
 */
public enum SerializationTypeEnum {
    /**
     * none
     */
    NONE("none", (byte) 0),
    /**
     * jdk
     */
    JDK("jdk", (byte) 1),
    /**
     * json
     */
    JSON("json", (byte) 2),
    /**
     * protostuff
     */
    PROTO_STUFF("protostuff", (byte) 4);

    private static final Map<Byte, SerializationTypeEnum> LOOK_UP = new HashMap<>();

    private final String name;
    private final byte value;

    SerializationTypeEnum(String name, byte value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public byte getValue() {
        return value;
    }

    public static SerializationTypeEnum get(byte value) {
        return LOOK_UP.get(value);
    }

    static {
        SerializationTypeEnum serializationTypeEnum;
        for (Object m : EnumSet.allOf(SerializationTypeEnum.class)) {
            serializationTypeEnum = (SerializationTypeEnum) m;
            LOOK_UP.put(serializationTypeEnum.value, serializationTypeEnum);
        }
    }
}
