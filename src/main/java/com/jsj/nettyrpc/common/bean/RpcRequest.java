package com.jsj.nettyrpc.common.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装 RPC 请求
 *
 * @author jsj
 * @date 2018-10-9
 */
@Data
@NoArgsConstructor
public class RpcRequest {

    /**
     * 请求id
     */
    private String requestId;
    /**
     * service名称
     */
    private String interfaceName;
    /**
     * service版本号
     */
    private String serviceVersion;
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
}
