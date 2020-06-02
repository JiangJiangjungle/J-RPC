package com.jsj.rpc.protocol;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.jsj.rpc.RpcCallback;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Method;

/**
 * @author jiangshenjie
 */
@Setter
@ToString
@NoArgsConstructor
public class RpcRequest implements Request {
    private Long requestId;
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

    public void setParams(Object... params) {
        this.params = params;
    }

    @Override
    public Method getMethod() {
        return this.method;
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
    public Long getRequestId() {
        return this.requestId;
    }

    @Override
    public RpcCallback<?> getCallback() {
        return this.callback;
    }
}
