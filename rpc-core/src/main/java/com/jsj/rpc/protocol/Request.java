package com.jsj.rpc.protocol;

import com.jsj.rpc.RpcCallback;

import java.lang.reflect.Method;

/**
 * @author jiangshenjie
 */
public interface Request {
    long getRequestId();

    Request setRequestId(long id);

    String getServiceName();

    Method getMethod();

    Object getTarget();

    Object[] getParams();

    RpcCallback<?> getCallback();

    String getMethodName();

    int getWriteTimeoutMillis();

    Request setWriteTimeoutMillis(int writeTimeoutMillis);

    int getTaskTimeoutMills();

    Request setTaskTimeoutMills(int taskTimeoutMills);

    Request setServiceName(String serviceName);

    Request setMethodName(String methodName);

    Request setMethod(Method method);

    Request setTarget(Object target);

    Request setParams(Object... params);

    Request setCallback(RpcCallback<?> callback);

    RpcMeta.RequestMeta transToRequestMeta();

    Packet transToPacket();
}
