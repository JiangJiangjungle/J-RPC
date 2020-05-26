package com.jsj.rpc;

/**
 * RpcRequest的回调函数
 *
 * @author jiangshenjie
 */
public interface RpcCallback<V> {
    /**
     * 回调处理response的返回结果
     *
     * @param result
     */
    void handleResult(V result);

    /**
     * 回调处理response的异常信息
     *
     * @param e
     */
    void handleException(Exception e);
}
