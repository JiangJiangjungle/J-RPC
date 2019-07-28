package com.jsj.rpc.sample.consumer.task;

import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.RpcProxy;
import com.jsj.rpc.protocol.RpcResponse;
import com.jsj.sample.api.service.HelloService;

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
        Method method = HelloService.class.getMethod("hello");
        long now = System.currentTimeMillis();
        RpcFuture<RpcResponse> future = rpcProxy.call(HelloService.class, method, null);
        RpcResponse rpcResponse = future.get();
        now = System.currentTimeMillis() - now;
        System.out.println(rpcResponse + " 耗时：" + now + " ms");
        return now;
    }
}
