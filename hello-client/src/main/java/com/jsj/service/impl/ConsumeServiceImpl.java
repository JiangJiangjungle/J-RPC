package com.jsj.service.impl;


import com.jsj.rpc.client.RpcProxy;
import com.jsj.rpc.common.RpcFuture;
import com.jsj.rpc.common.RpcResponse;
import com.jsj.service.ConsumeService;
import com.jsj.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

@Service("consumeService")
public class ConsumeServiceImpl implements ConsumeService {

    @Autowired
    private RpcProxy rpcProxy;

    @Override
    public String callHelloSync() {
        System.out.println("同步调用HelloService 的 hello方法: " + LocalTime.now());
        HelloService helloService = rpcProxy.getService(HelloService.class);
        String result = helloService.hello();
        System.out.println(result + ": " + LocalTime.now());
        return result;
    }

    @Override
    public String callHello() {
        System.out.println("异步调用HelloService 的 hello方法: " + LocalTime.now());
        try {
            Method method = HelloService.class.getMethod("hello");
            RpcFuture future = rpcProxy.call(HelloService.class, method, null);
            RpcResponse rpcResponse = future.get();
            System.out.println("now: " + LocalTime.now());
            String result = (String) rpcResponse.getServiceResult();
            System.out.println(result);
            return result;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String callTimeOutSync() {
        System.out.println("异步调用HelloService 的 timeOut方法: " + LocalTime.now());
        try {
            HelloService helloService = rpcProxy.getService(HelloService.class);
            String result = helloService.timeOut();
            System.out.println(result + ": " + LocalTime.now());
            return result;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String callTimeOut() {
        System.out.println("异步调用HelloService 的 timeOut方法: " + LocalTime.now());
        try {
            Method method = HelloService.class.getMethod("timeOut");
            RpcFuture future = rpcProxy.call(HelloService.class, method, null);
            RpcResponse rpcResponse = future.get(3000, TimeUnit.MILLISECONDS);
            String result = (String) rpcResponse.getServiceResult();
            System.out.println(result);
            return result;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String callAdd() {
        System.out.println("同步调用HelloService 的 hello方法: " + LocalTime.now());
        HelloService helloService = rpcProxy.getService(HelloService.class);
        int result = helloService.add(1,1);
        return ""+result;
    }
}
