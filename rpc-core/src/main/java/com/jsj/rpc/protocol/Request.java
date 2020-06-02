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

    void setServiceName(String serviceName);

    String getMethodName();

    void setMethodName(String methodName);

    void setMethod(Method method);

    void setTarget(Object target);

    void setParams(Object... params);

    void setCallback(RpcCallback<?> callback);
}
