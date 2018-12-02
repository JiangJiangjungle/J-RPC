package com.jsj.service.impl;


import com.jsj.rpc.common.RpcResponse;
import com.jsj.rpc.common.RpcFuture;
import com.jsj.rpc.client.RpcProxy;
import com.jsj.service.HelloService;
import com.jsj.service.ConsumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalTime;

@Service("consumeService")
public class ConsumeServiceImpl implements ConsumeService {

    @Autowired
    private RpcProxy rpcProxy;

    @Override
    public String callHelloSync() {
        System.out.println("同步调用HelloService 的 hello方法: "+ LocalTime.now());
        HelloService helloService = rpcProxy.getService(HelloService.class);
        String result = helloService.hello();
        System.out.println(result+": "+ LocalTime.now());
        return result;
    }

    @Override
    public String callHello() {
        System.out.println("异步调用HelloService 的 hello方法: "+ LocalTime.now());
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
