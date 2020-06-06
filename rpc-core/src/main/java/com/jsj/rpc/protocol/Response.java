package com.jsj.rpc.protocol;

import com.jsj.rpc.RpcFuture;

/**
 * @author jiangshenjie
 */
public interface Response {
    RpcFuture<?> getRpcFuture();

    void setRpcFuture(RpcFuture<?> rpcFuture);

    Exception getException();

    Object getResult();

    long getRequestId();

    void setRequestId(long requestId);

    void setException(Exception exception);

    void setResult(Object object);

    RpcMeta.ResponseMeta transToResponseMeta();

    Packet transToPacket();
}
