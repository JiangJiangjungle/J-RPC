package com.jsj.nettyrpc.common.bean;

import com.jsj.nettyrpc.common.constant.RpcResultEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装 RPC 响应
 *
 * @author huangyong
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
public class RpcResponse {

    /**
     * 请求id
     */
    private String requestId;
    /**
     * 请求调用结果
     */
    private RpcResultEnum rpcResultEnum;
    /**
     * RPC响应对象
     */
    private Object serviceResult;
}
