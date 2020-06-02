package com.jsj.rpc.protocol;

import com.jsj.rpc.RpcCallback;

import java.lang.reflect.Method;

/**
 * @author jiangshenjie
 */
public interface Request {
    long getRequestId();

    void setRequestId(long id);

    String getServiceName();

    Method getMethod();

    Object getTarget();

    Object[] getParams();

    RpcCallback<?> getCallback();

    String getMethodName();

    int getWriteTimeoutMillis();

    void setWriteTimeoutMillis(int writeTimeoutMillis);

    int getReadTimeoutMillis();

    void setReadTimeoutMillis(int readTimeoutMillis);

    void setServiceName(String serviceName);

    void setMethodName(String methodName);

    void setMethod(Method method);

    void setTarget(Object target);

    void setParams(Object... params);

    void setCallback(RpcCallback<?> callback);
}
