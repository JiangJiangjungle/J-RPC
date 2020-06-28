package com.jsj.rpc;

import lombok.*;

import java.lang.reflect.Method;

/**
 * @author jiangshenjie
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RpcMethodDetail {
    protected Method method;
    protected String methodName;
    /**
     * instance of interface which method belongs to
     */
    private Object target;
    private String serviceName;

    public RpcMethodDetail(Method method) {
        this.method = method;
        this.methodName = method.getName();
        this.serviceName = method.getDeclaringClass().getName();
    }

    public Class<?>[] getParamTypes() {
        return method.getParameterTypes();
    }
}
