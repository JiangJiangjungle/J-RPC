package com.jsj.rpc.exception;

/**
 * rpc调用异常
 */
public class RpcException extends Exception {
    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }
}
