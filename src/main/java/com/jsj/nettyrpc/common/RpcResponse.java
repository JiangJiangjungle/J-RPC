package com.jsj.nettyrpc.common;

import com.jsj.nettyrpc.common.RpcStateCode;
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
     * RPC响应对象
     */
    private Object serviceResult;
    /**
     * 错误信息
     */
    private String errorMsg;
}
