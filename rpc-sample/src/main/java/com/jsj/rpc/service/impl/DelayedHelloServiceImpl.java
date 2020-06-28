package com.jsj.rpc.service.impl;

import com.jsj.rpc.HelloRequest;
import com.jsj.rpc.HelloResponse;
import com.jsj.rpc.service.DelayedHelloService;
import com.jsj.rpc.service.HelloService;

/**
 * @author jiangshenjie
 */
public class DelayedHelloServiceImpl implements DelayedHelloService {
    HelloService helloService;

    public DelayedHelloServiceImpl(HelloService helloService) {
        this.helloService = helloService;
    }

    @Override
    public HelloResponse sayDelayedHello(HelloRequest helloRequest) {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return helloService.sayHello(helloRequest);
    }
}
