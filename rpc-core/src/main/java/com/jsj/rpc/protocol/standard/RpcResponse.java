package com.jsj.rpc.protocol.standard;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.protocol.Response;
import com.jsj.rpc.protocol.RpcMeta;
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

    @Override
    public RpcMeta.ResponseMeta createResponseMeta() {
        RpcMeta.ResponseMeta.Builder responseMetaBuilder = RpcMeta.ResponseMeta.newBuilder();
        responseMetaBuilder.setRequestId(getRequestId());
        if (result != null) {
            responseMetaBuilder.setResult(Any.pack((Message) result));
        }
        if (exception != null) {
            responseMetaBuilder.setErrMsg(exception.getMessage());
        }
        return responseMetaBuilder.build();
    }
}
