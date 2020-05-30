package com.jsj.rpc.client;

import com.jsj.rpc.HelloService;
import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.User;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jiangshenjie
 */
@Slf4j
public class DemoRpcClient {
    public static void main(String[] args) throws Exception {
        Endpoint serverInfo = new Endpoint("127.0.0.1", 2333);
        RpcClient client = testInvoke(serverInfo);
        client.shutdown();
    }

    public static RpcClient testInvoke(Endpoint serverInfo) throws Exception {
        RpcClient client = new RpcClient(serverInfo);
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
        return client;
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
