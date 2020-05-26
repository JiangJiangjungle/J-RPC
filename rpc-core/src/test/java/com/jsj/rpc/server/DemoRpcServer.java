package com.jsj.rpc.server;

import org.junit.Assert;
import org.mockito.Mockito;

public class DemoRpcServer {

    public static void main(String[] args) throws Exception {
        HelloService helloService = Mockito.mock(HelloService.class);
        Mockito.when(helloService.hello("jsj")).thenReturn("hello jsj!");
        String ip = "127.0.0.1";
        int port = 2333;
        RpcServer rpcServer = new RpcServer(ip, port);
        boolean started = rpcServer.start();
        Assert.assertTrue(started);
        rpcServer.registerService(helloService, HelloService.class);
        while (true) {
        }
    }
}
