package com.jsj.rpc;

import com.jsj.rpc.registry.ServiceDiscovery;

import javax.management.ServiceNotFoundException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiangshenjie
 */
public abstract class AbstractRpcProxy implements RpcProxy {
    private ServiceDiscovery serviceDiscovery;

    /**
     * rpc service代理对象列表(用于客户端的同步调用),避免重复创建
     */
    private ConcurrentHashMap<String, Object> serviceProxyInstanceMap = new ConcurrentHashMap<>(16);

    public AbstractRpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public <T> T getService(Class<? extends T> interfaceClass) throws Exception {
        String className = interfaceClass.getName();
        Object existedInstance = serviceProxyInstanceMap.get(className);
        if (existedInstance != null) return (T) existedInstance;
        //若不存在实例，获取对应服务的注册地址
        String addr = getRegistrationAddr(className);
        if (addr == null || "".equals(addr)) {
            throw new ServiceNotFoundException();
        }
        //若map中不存在实例，则根据Class对象和注册地址创建服务实例
        T serviceInstance = createInstance(interfaceClass, addr);
        if (serviceInstance == null) return null;
        this.serviceProxyInstanceMap.put(className, serviceInstance);
        return serviceInstance;
    }

    /**
     * 创建一个注册rpc服务的实例
     *
     * @param interfaceClass Class对象
     * @param addr           注册地址
     * @param <T>
     * @return
     */
    protected abstract <T> T createInstance(Class<? extends T> interfaceClass, String addr);

    /**
     * 获取注册地址
     *
     * @param className
     * @return
     * @throws Exception
     */
    protected String getRegistrationAddr(String className) throws Exception {
        String addr = this.serviceDiscovery.discover(className);
        if (addr == null || "".equals(addr)) return "";
        return addr;
    }
}
