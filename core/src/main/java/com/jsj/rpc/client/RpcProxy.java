package com.jsj.rpc.client;

import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.RpcResponse;

import java.lang.reflect.Method;

/**
 * 服务代理,需要实现同步与异步两种调用接口
 *
 * @author jiangshenjie
 */
public interface RpcProxy {

    /**
     * 获取一个注册的RPC service代理对象，用于同步调用
     *
     * @param interfaceClass
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T getService(final Class<? extends T> interfaceClass) throws Exception;

    /**
     * 异步调用rpc服务
     *
     * @return
     * @throws Exception
     */
    <T> RpcFuture<RpcResponse> call(final Class<? extends T> interfaceClass, Method method, Object[] parameters) throws Exception;


}
