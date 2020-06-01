package com.jsj.rpc.server;

import com.jsj.rpc.HelloService;
import com.jsj.rpc.HelloServiceImpl;

/**
 * @author jiangshenjie
 */
public class DemoRpcServer {

    public static void main(String[] args) throws Exception {
        HelloService helloService = new HelloServiceImpl();
        String ip = "127.0.0.1";
        int port = 2333;
        RpcServer rpcServer = new RpcServer(ip, port);
        boolean started = rpcServer.start();
        rpcServer.registerService(helloService, HelloService.class);
        while (true) {
        }
    }
}
