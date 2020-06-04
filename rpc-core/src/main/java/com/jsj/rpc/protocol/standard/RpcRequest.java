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
    private int writeTimeoutMillis;
    private int readTimeoutMillis;

    @Override
    public RpcMeta.RequestMeta createRequestMeta() {
        RpcMeta.RequestMeta.Builder metaBuilder = RpcMeta.RequestMeta.newBuilder();
        metaBuilder.setRequestId(getRequestId());
        metaBuilder.setServiceName(getServiceName());
        metaBuilder.setMethodName(getMethodName());
        for (Object param : getParams()) {
            metaBuilder.addParams(Any.pack((Message) param));
        }
        return metaBuilder.build();
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

    @Override
    public int getWriteTimeoutMillis() {
        return this.writeTimeoutMillis;
    }

    @Override
    public void setWriteTimeoutMillis(int writeTimeoutMillis) {
        this.writeTimeoutMillis = writeTimeoutMillis;
    }

    @Override
    public int getReadTimeoutMillis() {
        return this.readTimeoutMillis;
    }

    @Override
    public void setReadTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }
}
