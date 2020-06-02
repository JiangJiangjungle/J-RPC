package com.jsj.rpc.protocol;

import com.jsj.rpc.RpcFuture;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jiangshenjie
 */
@Setter
@ToString
@NoArgsConstructor
public class RpcResponse implements Response {
    private long requestId;
    private Object result;
    private Exception exception;
    private RpcFuture rpcFuture;

    @Override
    public RpcFuture getRpcFuture() {
        return this.rpcFuture;
    }

    @Override
    public Long getRequestId() {
        return this.requestId;
    }

    @Override
    public Exception getException() {
        return this.exception;
    }

    @Override
    public Object getResult() {
        return this.result;
    }
}
