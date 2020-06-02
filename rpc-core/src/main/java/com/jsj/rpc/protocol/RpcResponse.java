package com.jsj.rpc.protocol;

import com.jsj.rpc.RpcFuture;
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
public class RpcResponse implements Response {
    private long requestId;
    private Object result;
    private Exception exception;
    private RpcFuture rpcFuture;
}
