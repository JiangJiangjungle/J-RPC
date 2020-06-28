package com.jsj.rpc.service;

import com.jsj.rpc.HelloRequest;
import com.jsj.rpc.HelloResponse;
import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.RpcFuture;

/**
 * @author jiangshenjie
 */
public interface HelloServiceAsync extends HelloService {
    RpcFuture<HelloResponse> sayHello(HelloRequest helloRequest, RpcCallback<HelloResponse> callback);
}
