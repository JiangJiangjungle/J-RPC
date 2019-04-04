package com.jsj.rpc.protocol;

import com.jsj.rpc.protocol.Body;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 封装 RPC 请求
 *
 * @author jsj
 * @date 2018-10-9
 */
public class RpcRequest implements Body, Serializable {
    /**
     * 请求id
     */
    private Integer requestId;
    /**
     * service名称
     */
    private String interfaceName;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;
    /**
     * 参数
     */
    private Object[] parameters;

    public RpcRequest() {
    }

    public RpcRequest(Integer requestId, String interfaceName, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        this.requestId = requestId;
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestId=" + requestId +
                ", interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }
}
