package com.jsj.rpc.service;

import com.jsj.rpc.HelloRequest;
import com.jsj.rpc.HelloResponse;

/**
 * @author jiangshenjie
 */
public interface DelayedHelloService {
    HelloResponse sayDelayedHello(HelloRequest helloRequest);
}
