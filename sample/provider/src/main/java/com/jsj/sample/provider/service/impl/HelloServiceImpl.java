package com.jsj.sample.provider.service.impl;

import com.jsj.rpc.RpcService;
import com.jsj.sample.api.service.HelloService;
import org.springframework.stereotype.Service;

@Service
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return String.format("Hello %s", name);
    }
}
