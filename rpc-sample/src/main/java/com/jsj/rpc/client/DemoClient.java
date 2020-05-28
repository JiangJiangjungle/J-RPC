package com.jsj.rpc.client;

import com.jsj.rpc.HelloService;
import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.User;
import com.jsj.rpc.registry.LocalServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

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
        User.UserInfo.Builder userInfoBuilder = User.UserInfo.newBuilder();
        userInfoBuilder.setName("jsj");
        userInfoBuilder.setAge(20);
        userInfoBuilder.setId(1L);
        User.UserInfo userInfo = userInfoBuilder.build();
        RpcFuture<User.UserDetail> rpcFuture = client.invoke(HelloService.class, "hello", new DemoRpcCallback(), userInfo);
        log.info("rpc result: {}.", rpcFuture.get());
        //代理对象调用
        HelloService helloService = RpcClient.getProxy(client, HelloService.class);
        User.UserDetail userDetail = helloService.hello(userInfo);
        log.info("rpc result by proxy: {}.", userDetail);
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
