package com.jsj.rpc.exception;

/**
 * rpc调用方法不当所出现的异常
 *
 * @author jiangshenjie
 */
public class RpcCallException extends RuntimeException {
    private int code;

    public RpcCallException(int code) {
        this.code = code;
    }

    public RpcCallException(int code, String message) {
        super(message);
        this.code = code;
    }

    public RpcCallException(String message) {
        super(message);
    }
}
