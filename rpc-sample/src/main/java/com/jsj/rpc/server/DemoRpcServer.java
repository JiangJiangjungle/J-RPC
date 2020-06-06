package com.jsj.rpc.server;

import com.jsj.rpc.service.DelayedHelloService;
import com.jsj.rpc.service.DelayedHelloServiceImpl;
import com.jsj.rpc.service.HelloService;
import com.jsj.rpc.service.HelloServiceImpl;

/**
 * @author jiangshenjie
 */
public class DemoRpcServer {

    public static void main(String[] args) throws Exception {
        HelloService helloService = new HelloServiceImpl();
        DelayedHelloService delayedHelloService = new DelayedHelloServiceImpl(helloService);
        String ip = "127.0.0.1";
        int port = 2333;
        RpcServer rpcServer = new RpcServer(ip, port);
        boolean started = rpcServer.start();
        rpcServer.registerService(helloService, HelloService.class);
        rpcServer.registerService(delayedHelloService, DelayedHelloService.class);
        while (true) {
        }
    }
}
