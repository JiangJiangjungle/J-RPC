package com.jsj.rpc.common;


import com.jsj.rpc.client.RpcProxy;

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
     * 表示异步调用是否完成
     */
    private boolean done = false;

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
        return RpcProxy.futureMap.remove(requestId) != null;
    }

    @Override
    public boolean isCancelled() {
        return !RpcProxy.futureMap.containsKey(requestId);
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public RpcResponse get() throws InterruptedException, ExecutionException {
        if (done) {
            return rpcResponse;
        }
        try {
            lock.lock();
            while (!done) {
                waitUntilDone.await();
            }
        } finally {
            lock.unlock();
        }
        return rpcResponse;
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
        if (done) {
            return rpcResponse;
        }
        throw new TimeoutException();
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

}
