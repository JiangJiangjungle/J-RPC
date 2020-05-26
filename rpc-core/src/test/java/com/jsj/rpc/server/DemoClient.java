package com.jsj.rpc.server;

import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.client.Endpoint;
import com.jsj.rpc.client.RpcClient;
import com.jsj.rpc.protocol.RpcRequest;
import com.jsj.rpc.registry.ServiceDiscovery;
import org.mockito.Mockito;

public class DemoClient {
    public static void main(String[] args) throws Exception {
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        Mockito.when(serviceDiscovery.discover(HelloService.class.getName()))
                .thenReturn(new Endpoint("127.0.0.1", 2333));
        RpcClient client = new RpcClient(serviceDiscovery);
        client.init();
        RpcRequest request = new RpcRequest();
        request.setRequestId(0L);
        request.setServiceName(HelloService.class.getName());
        request.setMethod(HelloService.class.getDeclaredMethod("hello", String.class));
        request.setMethodName("hello");
        request.setParams("jsj");
        request.setCallback(new DemoRpcCallback());
        client.sendRequest(request);
        while (true) {
        }
    }

    private static class DemoRpcCallback implements RpcCallback<String> {
        @Override
        public void handleResult(String result) {
            System.out.printf("receive result: %s\n", result);
        }

        @Override
        public void handleException(Exception e) {
            System.out.printf("receive exception: %s\n", e.getMessage());
        }
    }
}
