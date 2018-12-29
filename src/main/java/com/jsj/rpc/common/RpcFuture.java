package com.jsj.rpc.common;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private Lock lock = new ReentrantLock();
    private Condition waitUntilDone = lock.newCondition();

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
        cancelled = true;
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
        RpcResponse rpcResponse;
        try {
            rpcResponse = this.get(MAX_WAIT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException t) {
            throw new ExecutionException(t.getCause());
        }
        return rpcResponse;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        if (!done && nanos > 0L) {
            long deadline = System.nanoTime() + nanos;
            long ns;
            try {
                lock.lock();
                while (!done && (ns = deadline - System.nanoTime()) > 0L) {
                    waitUntilDone.await(ns, TimeUnit.NANOSECONDS);
                }
            } finally {
                lock.unlock();
            }
        }
        if (done) {
            return rpcResponse;
        }
        throw new TimeoutException("调用超时!");
    }

    /**
     * Handler收到RpcResponse后调用此方法
     *
     * @param rpcResponse
     */
    public void done(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
        this.done = true;
        try {
            lock.lock();
            //唤醒同步等待响应的线程
            waitUntilDone.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public int getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return "RpcFuture{" +
                "done=" + done +
                ", lock=" + lock +
                ", waitUntilDone=" + waitUntilDone +
                ", rpcResponse=" + rpcResponse +
                ", requestId=" + requestId +
                '}';
    }
}
