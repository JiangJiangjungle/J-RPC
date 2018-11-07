package com.jsj.nettyrpc.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 封装 RPC 请求
 *
 * @author jsj
 * @date 2018-10-9
 */
@Data
@NoArgsConstructor
public class RpcRequest implements Serializable {

    private boolean heartBeat;
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
}
