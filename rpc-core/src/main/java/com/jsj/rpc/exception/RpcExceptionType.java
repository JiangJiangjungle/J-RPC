package com.jsj.rpc.exception;

/**
 * @author jiangshenjie
 */

public enum RpcExceptionType {
    UNKNOWN_EXCEPTION(0, "unknown"),
    REQUEST_EXCEPTION(1, "bad request"),
    TIMEOUT_EXCEPTION(2, "timeout"),
    SERVICE_EXCEPTION(3, "service exception");

    private int code;
    private String message;

    RpcExceptionType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
