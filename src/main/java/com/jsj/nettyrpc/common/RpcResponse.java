package com.jsj.nettyrpc.common;

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

    private boolean heartBeat;
    /**
     * 请求id
     */
    private Integer requestId;
    /**
     * RPC响应对象
     */
    private Object serviceResult;
    /**
     * 错误信息
     */
    private String errorMsg;
}
