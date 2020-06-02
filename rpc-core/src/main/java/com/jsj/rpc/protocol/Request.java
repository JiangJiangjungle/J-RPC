package com.jsj.rpc.protocol;

import com.jsj.rpc.RpcCallback;

import java.lang.reflect.Method;

/**
 * @author jiangshenjie
 */
public interface Request {
    Method getMethod();

    Object getTarget();

    Object[] getParams();

    Long getRequestId();

    RpcCallback<?> getCallback();
}
