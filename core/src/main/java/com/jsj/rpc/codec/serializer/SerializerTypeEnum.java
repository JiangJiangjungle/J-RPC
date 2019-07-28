package com.jsj.rpc.codec.serializer;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 序列化类型
 *
 * @author jsj
 * @date 2019-04-04
 */
public enum SerializerTypeEnum {
    /**
     * default
     */
    DEFAULT("default", (byte) 0, new ProtostuffSerializer()),
    /**
     * jdk
     */
    JDK("jdk", (byte) 1, new JDKSerializer()),
    /**
     * json
     */
    JSON("json", (byte) 2, new JSONSerializer()),
    /**
     * kryo
     */
    KRYO("kryo", (byte) 3, new ProtostuffSerializer()),
    /**
     * protostuff
     */
    PROTO_STUFF("proto_stuff", (byte) 4, new ProtostuffSerializer());

    private static final Map<Byte, SerializerTypeEnum> LOOK_UP = new HashMap<>();

    private final String name;
    private final byte value;
    private final Serializer serializer;

    SerializerTypeEnum(String name, byte value, Serializer serializer) {
        this.name = name;
        this.value = value;
        this.serializer = serializer;
    }

    public String getName() {
        return name;
    }

    public byte getValue() {
        return value;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public static SerializerTypeEnum get(byte value) {
        return LOOK_UP.get(value);
    }

    static {
        SerializerTypeEnum serializerTypeEnum;
        for (Object m : EnumSet.allOf(SerializerTypeEnum.class)) {
            serializerTypeEnum = (SerializerTypeEnum) m;
            LOOK_UP.put(serializerTypeEnum.value, serializerTypeEnum);
        }
    }
}
