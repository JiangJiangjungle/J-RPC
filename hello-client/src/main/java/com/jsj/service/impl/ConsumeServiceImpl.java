package com.jsj.service.impl;


import com.jsj.nettyrpc.common.client.RpcProxy;
import com.jsj.service.HelloService;
import com.jsj.service.ConsumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("consumeService")
public class ConsumeServiceImpl implements ConsumeService {

    @Autowired
    private RpcProxy rpcProxy;

    @Override
    public String callHello() {
        HelloService helloService = rpcProxy.getService(HelloService.class);
        String result = helloService.hello();
        System.out.println(result);
        return result;
    }
}
