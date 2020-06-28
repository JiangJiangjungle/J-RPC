package com.jsj.rpc.client;

import com.jsj.rpc.HelloRequest;
import com.jsj.rpc.HelloResponse;
import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.service.DelayedHelloService;
import com.jsj.rpc.service.DelayedHelloServiceAsync;
import com.jsj.rpc.service.HelloService;
import com.jsj.rpc.service.HelloServiceAsync;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * @author jiangshenjie
 */
@Slf4j
public class HelloRpcClient {
    public static void main(String[] args) {
        Endpoint serverInfo = new Endpoint("127.0.0.1", 2333);
        RpcClient client = new RpcClient(serverInfo);

        HelloRequest.Builder userInfoBuilder = HelloRequest.newBuilder();
        userInfoBuilder.setName("jsj");

        HelloRequest helloRequest = userInfoBuilder.build();
        HelloServiceAsync helloServiceAsync = RpcClient.getProxy(client, HelloServiceAsync.class);
        //同步调用
        blockingCall(helloServiceAsync, helloRequest);
        //异步调用
        asyncCall(helloServiceAsync, helloRequest);
        client.shutdown();

        //新的rpc client
        RpcClientOptions clientOptions = new RpcClientOptions();
        //超时时间设置为1000ms
        clientOptions.setRpcTaskTimeoutMillis(2000);
        client = new RpcClient(serverInfo, clientOptions);
        DelayedHelloServiceAsync delayedHelloServiceAsync = RpcClient.getProxy(client, DelayedHelloServiceAsync.class);
        //调用超时
        blockingCallAndTimeout(delayedHelloServiceAsync, helloRequest);
        client.shutdown();
    }

    private static void blockingCallAndTimeout(DelayedHelloService delayedHelloService, HelloRequest helloRequest) {
        HelloResponse helloResponse = delayedHelloService.sayDelayedHello(helloRequest);
        log.info("rpc result by blocking call of sayDelayedHello method: {}.", helloResponse);
    }

    private static void asyncCall(HelloServiceAsync helloServiceAsync, HelloRequest helloRequest) {
        RpcFuture<HelloResponse> rpcFuture = helloServiceAsync.sayHello(helloRequest, new HelloRpcCallback());
        try {
            rpcFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void blockingCall(HelloService helloService, HelloRequest helloRequest) {
        HelloResponse helloResponse = helloService.sayHello(helloRequest);
        log.info("rpc result by blocking call: {}.", helloResponse);
    }

    @Slf4j
    private static class HelloRpcCallback implements RpcCallback<HelloResponse> {
        @Override
        public void handleResult(HelloResponse result) {
            log.info("rpc callback handle result: {}.", result);
        }

        @Override
        public void handleException(Exception e) {
            log.info("rpc callback handle exception.", e);
        }
    }
}
