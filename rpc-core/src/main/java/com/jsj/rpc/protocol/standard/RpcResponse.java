package com.jsj.rpc.protocol.standard;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.protocol.Packet;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.Response;
import com.jsj.rpc.protocol.RpcMeta;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jiangshenjie
 */
@Setter
@ToString
public class RpcResponse implements Response {
    private Protocol protocol;
    private long requestId;
    private Object result;
    private Exception exception;
    private RpcFuture<?> rpcFuture;

    public RpcResponse(Protocol protocol) {
        this.protocol = protocol;
    }

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
    public RpcMeta.ResponseMeta transToResponseMeta() {
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

    @Override
    public Packet transToPacket() {
        return protocol.createPacket(transToResponseMeta().toByteArray());
    }
}
