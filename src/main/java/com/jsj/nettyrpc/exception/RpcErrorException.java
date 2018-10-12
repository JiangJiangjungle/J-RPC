package com.jsj.nettyrpc.exception;

public class RpcErrorException extends RuntimeException {
    public RpcErrorException() {
    }

    public RpcErrorException(String message) {
        super(message);
    }
}
