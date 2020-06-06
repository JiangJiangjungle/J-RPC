package com.jsj.rpc.client;

import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.User;
import com.jsj.rpc.protocol.Request;
import com.jsj.rpc.service.DelayedHelloService;
import com.jsj.rpc.service.HelloService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jiangshenjie
 */
@Slf4j
public class DemoRpcClient {
    public static void main(String[] args) throws Exception {
        Endpoint serverInfo = new Endpoint("127.0.0.1", 2333);
        RpcClient client = new RpcClient(serverInfo);

        User.UserInfo.Builder userInfoBuilder = User.UserInfo.newBuilder();
        userInfoBuilder.setName("jsj");
        userInfoBuilder.setAge(20);
        userInfoBuilder.setId(1L);

        User.UserInfo userInfo = userInfoBuilder.build();
        //RpcFuture
        RpcFuture<User.UserDetail> rpcFuture = client.invoke(HelloService.class
                , "hello", null, userInfo);
        //同步调用
        log.info("rpc result: {}.", rpcFuture.get());

        //Proxy
        HelloService helloService = RpcClient.getProxy(client, HelloService.class);
        //同步调用
        userInfo = userInfoBuilder.build();
        User.UserDetail userDetail = helloService.hello(userInfo);
        log.info("rpc result by proxy: {}.", userDetail);

        //Callback调用
        client.invoke(HelloService.class, "hello", new DemoRpcCallback(), userInfo);

        //超时处理
        Request request = client.buildRequest(DelayedHelloService.class, "hello"
                , new DemoRpcCallback(), userInfo);
        request.setTaskTimeoutMills(3000);
        request.setWriteTimeoutMillis(1000);
        rpcFuture = client.sendRequest(request);
        log.info("rpc of delayed service result: {}.", rpcFuture.get());
        client.shutdown();
    }

    @Slf4j
    private static class DemoRpcCallback implements RpcCallback<User.UserDetail> {
        @Override
        public void handleResult(User.UserDetail result) {
            log.info("rpc callback handle result: {}.", result);
        }

        @Override
        public void handleException(Exception e) {
            log.info("rpc callback handle exception.", e);
        }
    }
}
