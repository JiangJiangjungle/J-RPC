package com.jsj.task;

import com.jsj.rpc.client.RpcProxy;
import com.jsj.rpc.common.RpcFuture;
import com.jsj.service.HelloService;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class FutureTestTask implements Callable<Long> {

    private RpcProxy rpcProxy;
    private CountDownLatch countDownLatch;

    public FutureTestTask(RpcProxy rpcProxy, CountDownLatch countDownLatch) {
        this.rpcProxy = rpcProxy;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public Long call() throws Exception {
        countDownLatch.countDown();
        long now = System.currentTimeMillis();
        Method method = HelloService.class.getMethod("hello");
        RpcFuture future = rpcProxy.call(HelloService.class, method, null);
        now = System.currentTimeMillis() - now;
        System.out.println(future.get().toString() + " 耗时：" + now + " ms");
        return now;
    }
}
