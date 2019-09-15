package com.jsj.rpc.server;

/**
 * 业务线程池提供的回调函数
 *
 * @param <T>
 * @author jiangshenjie
 */
public interface TaskCallbackHandler<T> {
    /**
     * 回调
     *
     * @param response
     */
    void callback(T response);
}
