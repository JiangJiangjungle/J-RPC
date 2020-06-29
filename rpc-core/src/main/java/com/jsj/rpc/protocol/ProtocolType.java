package com.jsj.rpc.protocol;

/**
 * 协议类型
 *
 * @author jiangshenjie
 */

public enum ProtocolType {
    /**
     * 自定义的二进制协议
     */
    STANDARD("standard"),
    /**
     * Http协议
     */
    HTTP11("http1.1");

    private String name;

    ProtocolType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
