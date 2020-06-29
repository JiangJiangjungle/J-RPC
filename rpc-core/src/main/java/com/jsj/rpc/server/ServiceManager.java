package com.jsj.rpc.server;

import com.jsj.rpc.RpcMethodDetail;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangshenjie
 */
@Slf4j
public class ServiceManager {
    private static final ServiceManager INSTANCE = new ServiceManager();
    private Map<String, RpcMethodDetail> rpcMethodDetailMap = new HashMap<>(16);

    private ServiceManager() {
    }

    public static ServiceManager getInstance() {
        return INSTANCE;
    }

    public RpcMethodDetail getService(String serviceName, String methodName) {
        String serviceKey = buildServiceKey(serviceName, methodName);
        return rpcMethodDetailMap.get(serviceKey);
    }

    public void registerService(Object service) {
        Class<?> targetClass = service.getClass();
        Class<?>[] interfaces = targetClass.getInterfaces();
        if (interfaces.length != 1) {
            log.error("Service must implement one interface only");
            throw new RuntimeException("Service must implement one interface only");
        }
        registerService(service, interfaces[0]);
    }

    public void registerService(Object service, Class<?> targetInterface) {
        if (targetInterface == null) {
            Class<?> targetClass = service.getClass();
            Class<?>[] interfaces = targetClass.getInterfaces();
            if (interfaces.length != 1) {
                log.error("Service must implement one interface only");
                throw new RuntimeException("Service must implement one interface only");
            }
            targetInterface = interfaces[0];
        }
        Method[] methods = targetInterface.getDeclaredMethods();
        for (Method method : methods) {
            RpcMethodDetail methodInfo = new RpcMethodDetail();
            String serviceName = method.getDeclaringClass().getName();
            String methodName = method.getName();
            methodInfo.setMethod(method);
            methodInfo.setMethodName(methodName);
            methodInfo.setTarget(service);
            methodInfo.setServiceName(serviceName);
            String serviceKey = buildServiceKey(serviceName, methodName);
            rpcMethodDetailMap.put(serviceKey, methodInfo);
            log.info("Register service, serviceName={}, methodName={}",
                    methodInfo.getServiceName(), methodInfo.getMethodName());
        }
    }

    private String buildServiceKey(String serviceName, String methodName) {
        return String.format("%s.%s", serviceName.toLowerCase(), methodName.toLowerCase());
    }
}
