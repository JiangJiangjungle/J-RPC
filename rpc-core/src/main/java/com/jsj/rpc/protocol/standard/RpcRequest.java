package com.jsj.rpc.protocol.standard;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.protocol.Request;
import com.jsj.rpc.protocol.RpcMeta;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Method;

/**
 * @author jiangshenjie
 */
@ToString
@NoArgsConstructor
public class RpcRequest implements Request {
    private long requestId;
    private String serviceName;
    private String methodName;
    private Object[] params;
    private Method method;
    private Object target;
    private RpcCallback callback;

    public RpcRequest values(RpcMeta.RequestMeta meta) {
        setRequestId(meta.getRequestId());
        setServiceName(meta.getServiceName());
        setMethodName(meta.getMethodName());
        Object[] params = new Object[meta.getParamsCount()];
        for (int i = 0; i < params.length; i++) {
            params[i] = meta.getParams(i);
        }
        setParams(params);
        return this;
    }

    public RpcMeta.RequestMeta requestMeta() {
        RpcMeta.RequestMeta.Builder builder = RpcMeta.RequestMeta.newBuilder();
        builder.setRequestId(requestId);
        builder.setServiceName(serviceName);
        builder.setMethodName(methodName);
        for (Object param : params) {
            if (param instanceof Message) {
                builder.addParams(Any.pack((Message) param));
            } else {
                throw new RuntimeException("param Type must be Message!");
            }
        }
        return builder.build();
    }

    @Override
    public void setParams(Object... params) {
        this.params = params;
    }

    @Override
    public String getServiceName() {
        return this.serviceName;
    }

    @Override
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String getMethodName() {
        return this.methodName;
    }

    @Override
    public Object getTarget() {
        return this.target;
    }

    @Override
    public Object[] getParams() {
        return this.params;
    }

    @Override
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public RpcCallback<?> getCallback() {
        return this.callback;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public long getRequestId() {
        return this.requestId;
    }

    @Override
    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public void setTarget(Object target) {
        this.target = target;
    }

    @Override
    public void setCallback(RpcCallback<?> callback) {
        this.callback = callback;
    }
}
