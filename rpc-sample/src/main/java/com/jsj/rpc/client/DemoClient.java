package com.jsj.rpc.client;

import com.jsj.rpc.HelloService;
import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.registry.LocalServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author jiangshenjie
 */
@Slf4j
public class DemoClient {
    public static void main(String[] args) throws Exception {
        LocalServiceDiscovery serviceDiscovery = new LocalServiceDiscovery();
        serviceDiscovery.addEndpoint(HelloService.class.getName(), new Endpoint("127.0.0.1", 2333));
        RpcClient client = new RpcClient(serviceDiscovery);
        client.init();
        //直接调用
        client.invoke(HelloService.class, "hello", new DemoRpcCallback(), "wsh");
        //代理对象调用
        HelloService helloService = RpcClient.getProxy(client, HelloService.class);
        String result = helloService.hello("jsj");
        log.info("rpc result by proxy: {}.", result);
        client.shutdown();
    }

    @Slf4j
    private static class DemoRpcCallback implements RpcCallback<String> {
        @Override
        public void handleResult(String result) {
            log.info("rpc callback handle result: {}.", result);
        }

        @Override
        public void handleException(Exception e) {
            log.info("rpc callback handle exception.", e);
        }
    }
}
