package com.jsj.rpc.protocol.standard;

import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.protocol.Response;
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
    private RpcFuture<?> rpcFuture;

    @Override
    public RpcFuture<?> getRpcFuture() {
        return this.rpcFuture;
    }

    @Override
    public long getRequestId() {
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
