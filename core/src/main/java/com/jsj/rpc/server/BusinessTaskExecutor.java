package com.jsj.rpc.server;

import com.jsj.rpc.RpcRequest;
import com.jsj.rpc.RpcResponse;

/**
 * 业务线程池
 *
 * @author jiangshenjie
 */
public interface BusinessTaskExecutor {

    /**
     * 在业务线程池执行任务
     *
     * @param rpcRequest
     * @param callback   回调函数
     */
    void execute(RpcRequest rpcRequest, TaskCallbackHandler<RpcResponse> callback);
}
