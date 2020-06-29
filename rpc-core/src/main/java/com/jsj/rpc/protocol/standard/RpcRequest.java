package com.jsj.rpc.protocol.standard;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.protocol.Packet;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.Request;
import com.jsj.rpc.protocol.RpcMeta;
import lombok.ToString;

import java.lang.reflect.Method;

/**
 * @author jiangshenjie
 */
@ToString
public class RpcRequest implements Request {
    private final Protocol protocol;
    private long requestId;
    private String serviceName;
    private String methodName;
    private Object[] params;
    private Method method;
    private Object target;
    private RpcCallback callback;
    private int writeTimeoutMillis;
    private int taskTimeoutMillis;

    public RpcRequest(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public RpcMeta.RequestMeta transToRequestMeta() {
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
    public Request setParams(Object... params) {
        this.params = params;
        return this;
    }

    @Override
    public String getServiceName() {
        return this.serviceName;
    }

    @Override
    public Request setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
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
    public Request setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
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
    public Request setMethod(Method method) {
        this.method = method;
        return this;
    }

    @Override
    public long getRequestId() {
        return this.requestId;
    }

    @Override
    public Request setRequestId(long requestId) {
        this.requestId = requestId;
        return this;
    }

    @Override
    public Request setTarget(Object target) {
        this.target = target;
        return this;
    }

    @Override
    public Request setCallback(RpcCallback<?> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public int getWriteTimeoutMillis() {
        return this.writeTimeoutMillis;
    }

    @Override
    public Request setWriteTimeoutMillis(int writeTimeoutMillis) {
        this.writeTimeoutMillis = writeTimeoutMillis;
        return this;
    }

    @Override
    public int getTaskTimeoutMills() {
        return taskTimeoutMillis;
    }

    @Override
    public Request setTaskTimeoutMills(int taskTimeoutMills) {
        this.taskTimeoutMillis = taskTimeoutMills;
        return this;
    }

    @Override
    public Packet transToPacket() {
        return protocol.createPacket(transToRequestMeta().toByteArray());
    }
}
