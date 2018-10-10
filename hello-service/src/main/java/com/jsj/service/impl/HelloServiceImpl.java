package com.jsj.service.impl;

import com.jsj.nettyrpc.server.RpcService;
import com.jsj.service.HelloService;
import org.springframework.stereotype.Component;

@Component
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello() {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "HELLO WORLD!";
    }
}
