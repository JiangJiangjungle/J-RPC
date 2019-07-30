package com.jsj.rpc;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 用于rpc框架的异步调用
 *
 * @author jsj
 * @date 2018-10-14
 */
public class DefaultRpcFuture implements RpcFuture<RpcResponse> {

    private final int requestId;
    /**
     * 异步调用完成
     */
    private boolean done = false;
    /**
     * 异步调用取消
     */
    private boolean cancelled = false;
    /**
     * 执行结果
     */
    private RpcResponse result;

    public DefaultRpcFuture(int requestId) {
        this.requestId = requestId;
    }

    @Override
    public boolean cancel() {
        synchronized (this) {
            this.notifyAll();
            this.cancelled = true;
        }
        return true;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public RpcResponse get() throws Exception {
        synchronized (this) {
            if (this.cancelled) {
                return null;
            }
            while (!this.done) {
                this.wait();
            }
        }
        return this.result;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws Exception {
        long millis = unit.toMillis(timeout);
        synchronized (this) {
            if (this.cancelled) {
                return null;
            }
            while (!this.done && millis > 0L) {
                this.wait(millis);
            }
        }
        if (!this.done) {
            throw new TimeoutException("调用超时!");
        }
        return this.result;
    }

    /**
     * RpcFuture获得RpcResponse后调用此方法
     *
     * @param result
     */
    public void done(RpcResponse result) {
        if (this.cancelled) return;
        this.result = result;
        this.done = true;
        synchronized (this) {
            this.notifyAll();
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public int getRequestId() {
        return this.requestId;
    }
}
