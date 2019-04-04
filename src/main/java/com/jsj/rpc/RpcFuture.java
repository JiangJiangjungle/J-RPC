package com.jsj.rpc;


import com.jsj.rpc.protocol.RpcResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 用于rpc框架的异步调用
 *
 * @author jsj
 * @date 2018-10-14
 */
public class RpcFuture implements Future<RpcResponse> {

    /**
     * get方法的最大等待时间
     */
    public static int MAX_WAIT_MS = 10000;

    /**
     * 表示异步调用是否完成
     */
    private volatile boolean done = false;
    private volatile boolean cancelled = false;

    /**
     * 返回响应
     */
    private RpcResponse rpcResponse;

    private final int requestId;

    public RpcFuture(int requestId) {
        this.requestId = requestId;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (this) {
            this.notifyAll();
            cancelled = true;
        }
        return cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public RpcResponse get() throws InterruptedException, ExecutionException {
        synchronized (this) {
            if (cancelled) {
                return null;
            }
            while (!done) {
                this.wait();
            }
        }
        return rpcResponse;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long millis = unit.toMillis(timeout);
        synchronized (this) {
            if (cancelled) {
                return null;
            }
            while (!done && millis > 0L) {
                this.wait(millis);
            }
        }
        if (done) {
            return rpcResponse;
        }
        throw new TimeoutException("调用超时!");
    }

    /**
     * RpcFuture获得RpcResponse后调用此方法
     *
     * @param rpcResponse
     */
    public void done(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
        this.done = true;
        synchronized (this) {
            this.notifyAll();
        }
    }

    public int getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
