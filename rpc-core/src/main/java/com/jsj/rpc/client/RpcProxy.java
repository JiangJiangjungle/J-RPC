package com.jsj.rpc.client;

import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.RpcMethodDetail;
import com.jsj.rpc.protocol.Request;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jiangshenjie
 */
@Slf4j
public class RpcProxy<T> implements MethodInterceptor {
    private static final Set<String> notProxyMethodSet = new HashSet<>();

    static {
        notProxyMethodSet.add("getClass");
        notProxyMethodSet.add("hashCode");
        notProxyMethodSet.add("equals");
        notProxyMethodSet.add("clone");
        notProxyMethodSet.add("toString");
        notProxyMethodSet.add("notify");
        notProxyMethodSet.add("notifyAll");
        notProxyMethodSet.add("wait");
        notProxyMethodSet.add("finalize");
    }

    private final RpcClient rpcClient;

    /**
     * 只保存sync方法
     */
    private Map<String, RpcMethodDetail> rpcMethodMap = new HashMap<>();

    private RpcProxy(RpcClient rpcClient, Class<T> clazz) {
        this.rpcClient = rpcClient;
        for (Method method : clazz.getMethods()) {
            if (notProxyMethodSet.contains(method.getName())) {
                log.debug("{}:{} does not need to proxy",
                        method.getDeclaringClass().getName(), method.getName());
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            int parameterNumber = parameterTypes.length;
            if (parameterNumber > 0) {
                Class<?> lastParameterType = parameterTypes[parameterNumber - 1];
                if (RpcCallback.class.isAssignableFrom(lastParameterType)) {
                    continue;
                }
            }
            Class<?> returnType = method.getReturnType();
            if (RpcFuture.class.isAssignableFrom(returnType)) {
                continue;
            }
            //only register sync method
            rpcMethodMap.put(method.getName(), new RpcMethodDetail(method));
            log.debug("client serviceName={}, methodName={}",
                    method.getDeclaringClass().getName(), method.getName());
        }
    }

    protected static <T> T getProxy(RpcClient rpcClient, Class<T> clazz) {
        rpcClient.setServiceInterface(clazz);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new RpcProxy<>(rpcClient, clazz));
        return (T) enhancer.create();
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        String methodName = method.getName();
        RpcMethodDetail rpcMethodDetail = rpcMethodMap.get(methodName);
        if (rpcMethodDetail == null) {
            log.debug("{}:{} does not need to proxy",
                    method.getDeclaringClass().getName(), methodName);
            return methodProxy.invokeSuper(obj, args);
        }
        RpcCallback<?> callback = null;
        int argNumber = args.length;
        if (argNumber > 0) {
            Object lastArg = args[argNumber - 1];
            if (lastArg instanceof RpcCallback) {
                argNumber--;
                callback = (RpcCallback<?>) lastArg;
                Object[] realArgs = new Object[argNumber];
                System.arraycopy(args, 0, realArgs, 0, argNumber);
                args = realArgs;
            }
        }
        Request request = rpcClient.buildRequest(rpcMethodDetail.getMethod(), callback, args);
        RpcFuture<?> rpcFuture = rpcClient.sendRequest(request);
        return shouldReturnFuture(method) ? rpcFuture : rpcFuture.get();
    }

    private boolean shouldReturnFuture(Method method) {
        return method.getReturnType().isAssignableFrom(RpcFuture.class);
    }
}
