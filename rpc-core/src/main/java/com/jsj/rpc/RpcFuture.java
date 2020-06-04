package com.jsj.rpc;

import com.jsj.rpc.protocol.Request;
import com.jsj.rpc.protocol.Response;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author jiangshenjie
 */
public class RpcFuture<T> implements Future<T> {
    private final Request request;
    private volatile boolean cancelled = false;
    private boolean isDone = false;
    private Response response;

    public RpcFuture(Request request) {
        this.request = request;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (this) {
            this.cancelled = true;
            this.isDone = true;
            this.notifyAll();
        }
        return true;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        synchronized (this) {
            while (!isDone()) {
                this.wait();
            }
        }
        if (isCancelled()) {
            throw new InterruptedException("rpc task cancelled.");
        }
        return (T) this.response.getResult();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException
            , ExecutionException, TimeoutException {
        long millis = unit.toMillis(timeout);
        synchronized (this) {
            while (!isDone() && millis > 0L) {
                this.wait(millis);
            }
        }
        if (!isDone()) {
            throw new TimeoutException();
        } else if (isCancelled()) {
            throw new InterruptedException("rpc task cancelled.");
        }
        return (T) this.response.getResult();
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isDone() {
        return this.isDone;
    }

    public RpcFuture<T> handleResponse(Response response) {
        if (isDone()) {
            return this;
        }
        synchronized (this) {
            if (isDone()) {
                return this;
            }
            RpcCallback<T> callback = (RpcCallback<T>) request.getCallback();
            if (callback != null) {
                if (response.getException() != null) {
                    callback.handleException(response.getException());
                } else {
                    callback.handleResult((T) response.getResult());
                }
            }
            this.response = response;
            this.isDone = true;
            this.notifyAll();
        }
        return this;
    }

    public Request getRequest() {
        return request;
    }
}
