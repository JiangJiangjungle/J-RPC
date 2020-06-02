package com.jsj.rpc.protocol;

import com.jsj.rpc.RpcFuture;

/**
 * @author jiangshenjie
 */
public interface Response {
    RpcFuture getRpcFuture();

    Long getRequestId();

    Exception getException();

    Object getResult();
}
