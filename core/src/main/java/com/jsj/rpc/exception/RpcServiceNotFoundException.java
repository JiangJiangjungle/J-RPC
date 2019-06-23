package com.jsj.rpc.exception;

/**
 * RPC 服务未发现时抛出该异常
 */
public class RpcServiceNotFoundException extends Exception {
    public RpcServiceNotFoundException() {
    }

    public RpcServiceNotFoundException(String message) {
        super(message);
    }
}
