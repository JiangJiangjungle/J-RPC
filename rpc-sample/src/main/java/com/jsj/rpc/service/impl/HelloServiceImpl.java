package com.jsj.rpc.service.impl;

import com.jsj.rpc.HelloRequest;
import com.jsj.rpc.HelloResponse;
import com.jsj.rpc.User;
import com.jsj.rpc.service.HelloService;

/**
 * @author jiangshenjie
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public HelloResponse sayHello(HelloRequest helloRequest) {
        HelloResponse.Builder builder = HelloResponse.newBuilder();
        builder.setMessage(String.format("Hello %s!", helloRequest.getName()));
        return builder.build();
    }
}
