package com.jsj.rpc.protocol;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author jiangshenjie
 */
@Setter
@Getter
@NoArgsConstructor
public class RpcResponse {
    private long requestId;
    private Object result;
    private Exception exception;
}
