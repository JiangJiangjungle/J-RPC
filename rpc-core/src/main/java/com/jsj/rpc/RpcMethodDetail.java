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
    private Object target;
    private String serviceName;
}
