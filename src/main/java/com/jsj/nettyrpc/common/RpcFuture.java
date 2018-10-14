package com.jsj.nettyrpc.common;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 用于rpc框架的异步调用
 */
public class RpcFuture<RpcResponse> implements Future<RpcResponse> {
    /**
     * 表示异步调用是否完成
     */
    private boolean done = false;

    /**
     * 返回响应
     */
    private RpcResponse rpcResponse;

    private String requestId;

    public RpcFuture(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public RpcResponse get() throws InterruptedException, ExecutionException {
        return done ? (RpcResponse) rpcResponse : null;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        if (!done && nanos > 0L) {
            long deadline = System.nanoTime() + nanos;
            long ns, ms;
            while (!done && (ns = deadline - System.nanoTime()) > 0L) {
                ms = TimeUnit.NANOSECONDS.toMillis(ns);
                this.wait(ms);
            }
        }
        return done ? rpcResponse : null;
    }

    public RpcResponse getRpcResponse() {
        return rpcResponse;
    }

    public void setRpcResponse(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
