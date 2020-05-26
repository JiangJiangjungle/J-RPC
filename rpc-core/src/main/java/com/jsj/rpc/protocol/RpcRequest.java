package com.jsj.rpc.protocol;

import com.jsj.rpc.RpcCallback;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;

/**
 * @author jiangshenjie
 */
@Setter
@Getter
@NoArgsConstructor
public class RpcRequest {
    private Long requestId;
    private String serviceName;
    private String methodName;
    private Object[] params;
    private Method method;
    private Object target;
    private RpcCallback callback;

    public RpcRequest values(RequestMeta meta) {
        setRequestId(meta.getRequestId());
        setServiceName(meta.getServiceName());
        setMethodName(meta.getMethodName());
        setParams(meta.getParams());
        return this;
    }

    public void setParams(Object... params) {
        this.params = params;
    }
}
