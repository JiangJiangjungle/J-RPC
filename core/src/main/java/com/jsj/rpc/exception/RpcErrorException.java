package com.jsj.rpc.exception;

/**
 * rpc调用异常
 */
public class RpcErrorException extends Exception {
    public RpcErrorException() {
    }

    public RpcErrorException(String message) {
        super(message);
    }
}
