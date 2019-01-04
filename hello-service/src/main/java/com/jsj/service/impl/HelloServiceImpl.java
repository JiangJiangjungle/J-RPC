package com.jsj.service.impl;

import com.jsj.rpc.RpcService;
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

    @Override
    public String timeOut() {
        try {
            Thread.sleep(12000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "time out";
    }

    @Override
    public int add(int x, int y) {
        return x + y;
    }
}
