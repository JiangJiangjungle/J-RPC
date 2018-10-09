package com.jsj.nettyrpc.common.constant;

/**
 * 用于返回RPC 结果
 *
 * @author jsj
 * @date 2018-10-9
 */
public enum RpcResultEnum {

    // 成功
    SUCCESS(1, "调用rpc成功"),
    // 失败
    FAIL(0, "调用rpc失败");

    private Integer code;
    private String value;

    RpcResultEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
