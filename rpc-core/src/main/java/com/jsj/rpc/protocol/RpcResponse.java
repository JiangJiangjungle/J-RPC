package com.jsj.rpc.protocol;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jiangshenjie
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class RpcResponse {
    private long requestId;
    private Object result;
    private Exception exception;
}
