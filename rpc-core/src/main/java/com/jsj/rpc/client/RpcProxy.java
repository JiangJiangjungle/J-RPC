package com.jsj.rpc.client;

import com.jsj.rpc.protocol.Request;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author jiangshenjie
 */
public class RpcProxy<T> implements MethodInterceptor {
    private RpcClient rpcClient;
    private Class<T> clazz;

    private RpcProxy(RpcClient rpcClient, Class<T> clazz) {
        this.rpcClient = rpcClient;
        this.clazz = clazz;
    }

    protected static <T> T getProxy(RpcClient rpcClient, Class<T> clz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clz);
        enhancer.setCallback(new RpcProxy<>(rpcClient, clz));
        return (T) enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Request request = rpcClient.buildRequest(clazz, method, null, objects);
        return rpcClient.sendRequest(request).get();
    }
}
