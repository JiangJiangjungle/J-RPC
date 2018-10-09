package com.jsj.nettyrpc.common.bean;

import com.jsj.nettyrpc.common.constant.RpcStateCode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装 RPC 响应
 *
 * @author jsj
 * @date 2018-10-9
 */
@Data
@NoArgsConstructor
public class RpcResponse {

    /**
     * 请求id
     */
    private String requestId;
    /**
     * 状态码
     */
    private RpcStateCode rpcStateCode;
    /**
     * RPC响应对象
     */
    private Object serviceResult;
}
