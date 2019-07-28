package com.jsj.rpc;

import java.util.concurrent.TimeUnit;

/**
 * @param <V>
 * @author jiangshenjie
 */
public interface RpcFuture<V> {

    /**
     * 返回执行结果
     *
     * @return
     * @throws Exception
     */
    V get() throws Exception;

    /**
     * 一定时间内返回执行结果
     *
     * @param timeout
     * @param unit
     * @return
     * @throws Exception
     */
    V get(long timeout, TimeUnit unit) throws Exception;

    /**
     * 操作是否完成
     *
     * @return
     */
    boolean isDone();

    /**
     * 操作取消
     *
     * @return
     */
    boolean cancel();

    /**
     * 操作是否已取消
     *
     * @return
     */
    boolean isCancelled();
}
