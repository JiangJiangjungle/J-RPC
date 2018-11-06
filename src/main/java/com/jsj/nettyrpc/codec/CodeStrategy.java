package com.jsj.nettyrpc.codec;

public enum CodeStrategy {
    JDK("jdk",0),
    JSON("json",1),
    PROTO_STUFF("protostuff",2),
    DEAULT("default",3);


    private String name;
    private int value;
    // 构造方法
    private CodeStrategy(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
}
