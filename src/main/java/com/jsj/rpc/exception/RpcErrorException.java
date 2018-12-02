package com.jsj.rpc.exception;

public class RpcErrorException extends RuntimeException {
    public RpcErrorException() {
    }

    public RpcErrorException(String message) {
        super(message);
    }
}
