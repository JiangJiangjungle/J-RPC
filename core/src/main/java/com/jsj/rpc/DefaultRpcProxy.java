package com.jsj.rpc;


import com.jsj.rpc.client.RpcClient;
import com.jsj.rpc.config.DefaultClientConfiguration;
import com.jsj.rpc.exception.RpcErrorException;
import com.jsj.rpc.protocol.RpcResponse;
import com.jsj.rpc.registry.ServiceDiscovery;
import io.netty.channel.ConnectTimeoutException;
import net.sf.cglib.proxy.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ServiceNotFoundException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC 代理（用于创建 RPC 服务代理）
 *
 * @author jsj
 * @date 2018-10-10
 */
public class DefaultRpcProxy extends AbstractRpcProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRpcProxy.class);
    /**
     * 客户端连接配置
     */
    private final DefaultClientConfiguration configuration;
    /**
     * rpc服务 ip:port->rpcClient 单例映射列表
     */
    private ConcurrentHashMap<String, RpcClient> rpcClientMap;

    public DefaultRpcProxy(ServiceDiscovery serviceDiscovery) {
        this(serviceDiscovery, new DefaultClientConfiguration());
    }

    public DefaultRpcProxy(ServiceDiscovery serviceDiscovery, DefaultClientConfiguration configuration) {
        super(serviceDiscovery);
        this.configuration = configuration;
        this.rpcClientMap = new ConcurrentHashMap<>(16);
    }

    @Override
    public <T> RpcFuture<RpcResponse> call(Class<? extends T> interfaceClass, Method method, Object[] parameters) throws Exception {
        String interfaceClassName = interfaceClass.getName();
        RpcClient client = this.getRpcClient(interfaceClassName);
        return client.invokeWithFuture(method, parameters);
    }

    @Override
    protected <T> T createInstance(Class<? extends T> interfaceClass, String addr) {
        // 创建代理实例
        T instance = (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                (proxy, method, parameters) -> {
                    RpcResponse rpcResponse;
                    String interfaceClassName = interfaceClass.getName();
                    RpcClient client = this.getRpcClient(interfaceClassName, addr, false);
                    try {
                        //进行同步调用
                        rpcResponse = client.invokeSync(method, parameters);
                    } catch (ConnectTimeoutException e) {
                        //重新查询服务地址
                        String newAddr = getRegistrationAddr(interfaceClassName);
                        if (newAddr == null || "".equals(newAddr)) throw new ServiceNotFoundException();
                        //重新初始化RpcClient
                        client = this.getRpcClient(interfaceClassName, newAddr, true);
                        rpcResponse = client.invokeSync(method, parameters);
                    } catch (Exception t) {
                        throw new RpcErrorException(t.getMessage());
                    }
                    // 返回 RPC 响应结果
                    if (null == rpcResponse || null != rpcResponse.getErrorMsg()) {
                        throw new RpcErrorException(null == rpcResponse ? null : rpcResponse.getErrorMsg());
                    }
                    return rpcResponse.getServiceResult();
                }
        );
        return instance;
    }

    /**
     * 获取RpcClient对象，需保证线程安全
     *
     * @param interfaceClassName
     * @return
     */
    private RpcClient getRpcClient(String interfaceClassName) throws Exception {
        RpcClient rpcClient = this.rpcClientMap.get(interfaceClassName);
        if (rpcClient != null) return rpcClient;
        String addr = getRegistrationAddr(interfaceClassName);
        return getRpcClient(interfaceClassName, addr, true);
    }

    /**
     * 创建RpcClient对象，需保证线程安全
     *
     * @param interfaceClassName
     * @param addr
     * @param forceCreate        强制重新创建RpcClient对象
     * @return
     */
    private RpcClient getRpcClient(String interfaceClassName, String addr, boolean forceCreate) {
        RpcClient rpcClient;
        if (!forceCreate && (rpcClient = this.rpcClientMap.get(interfaceClassName)) != null) {
            return rpcClient;
        }
        //加锁以避免并发创建
        synchronized (this) {
            if (!forceCreate && (rpcClient = this.rpcClientMap.get(interfaceClassName)) != null) {
                return rpcClient;
            }
            String[] split = addr.split(":");
            rpcClient = new RpcClient(split[0], Integer.parseInt(split[1]), configuration);
            this.rpcClientMap.put(interfaceClassName, rpcClient);
        }
        return rpcClient;
    }
}
