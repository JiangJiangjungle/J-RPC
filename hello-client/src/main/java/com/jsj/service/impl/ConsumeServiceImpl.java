package com.jsj.service.impl;


import com.jsj.nettyrpc.common.RpcResponse;
import com.jsj.nettyrpc.common.RpcFuture;
import com.jsj.nettyrpc.common.client.RpcProxy;
import com.jsj.service.HelloService;
import com.jsj.service.ConsumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

@Service("consumeService")
public class ConsumeServiceImpl implements ConsumeService {

    @Autowired
    private RpcProxy rpcProxy;

    @Override
    public String callHelloSync() {
        System.out.println("同步调用HelloService 的 hello方法");
        HelloService helloService = rpcProxy.getService(HelloService.class);
        String result = helloService.hello();
        System.out.println(result);
        return result;
    }

    @Override
    public String callHello() {
        System.out.println("异步调用HelloService 的 hello方法");
        try {
            Method method = HelloService.class.getMethod("hello");
            RpcFuture future = rpcProxy.call(HelloService.class, method, null);
            RpcResponse rpcResponse = future.get();
            String result = (String) rpcResponse.getServiceResult();
            System.out.println(result);
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
