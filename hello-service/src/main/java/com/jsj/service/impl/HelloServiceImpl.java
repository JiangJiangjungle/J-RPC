package com.jsj.service.impl;

import com.jsj.nettyrpc.RpcService;
import com.jsj.service.HelloService;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService, Serializable {
    @Override
    public String hello() {
        return "HELLO WORLD!";
    }
}
