package com.jsj.rpc.server;

import com.jsj.rpc.service.DelayedHelloService;
import com.jsj.rpc.service.HelloService;
import com.jsj.rpc.service.impl.DelayedHelloServiceImpl;
import com.jsj.rpc.service.impl.HelloServiceImpl;

/**
 * @author jiangshenjie
 */
public class HelloRpcServer {

    public static void main(String[] args) throws Exception {
        String ip = "127.0.0.1";
        int port = 2333;
        RpcServer rpcServer = new RpcServer(ip, port);

        HelloService helloService = new HelloServiceImpl();
        rpcServer.registerService(new HelloServiceImpl(), HelloService.class);
        rpcServer.registerService(new DelayedHelloServiceImpl(helloService), DelayedHelloService.class);

        boolean started = rpcServer.start();
        while (started) {
        }
    }
}
