package com.jsj.rpc.client;


import com.jsj.rpc.RpcStateCode;
import com.jsj.rpc.codec.CodeC;
import com.jsj.rpc.codec.CodeStrategy;
import com.jsj.rpc.codec.DefaultCodeC;
import com.jsj.rpc.common.RpcFuture;
import com.jsj.rpc.common.RpcResponse;
import com.jsj.rpc.exception.RpcErrorException;
import com.jsj.rpc.exception.RpcServiceNotFoundException;
import com.jsj.rpc.registry.ServiceDiscovery;
import com.jsj.rpc.util.StringUtil;
import io.netty.channel.ConnectTimeoutException;
import net.sf.cglib.proxy.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC 代理（用于创建 RPC 服务代理）
 *
 * @author jsj
 * @date 2018-10-10
 */
public class RpcProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);
    /**
     * 连接相关配置 单位: ms
     */
    public static int CONNECTION_READ_IDLE = 20000;
    public static int CONNECTION_WRITE_IDLE = 10000;

    private ServiceDiscovery serviceDiscovery;
    /**
     * rpc service代理对象列表(用于客户端的同步调用),避免重复创建
     */
    private ConcurrentHashMap<String, Object> serviceProxyInstanceMap = new ConcurrentHashMap<>(16);

    /**
     * rpc服务的远程地址的本地缓存，减少查询zookeeper
     */
    private ConcurrentHashMap<String, String> addressCache = new ConcurrentHashMap<>(16);

    /**
     * rpc服务 ip:port->rpcClient 映射列表,避免重复创建
     */
    private ConcurrentHashMap<String, RpcClient> rpcClientMap = new ConcurrentHashMap<>(16);

    /**
     * 编解码选项
     */
    private final CodeC codeC;

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this(serviceDiscovery, CodeStrategy.DEAULT);
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery, CodeStrategy codeStrategy) {
        this.serviceDiscovery = serviceDiscovery;
        this.codeC = new DefaultCodeC(codeStrategy);
    }

    /**
     * 获取RPC service的代理对象，用于同步调用
     *
     * @param interfaceClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(final Class<?> interfaceClass) {
        String interfaceName = interfaceClass.getName();
        T instance = (T) serviceProxyInstanceMap.get(interfaceName);
        if (instance == null) {
            instance = this.create(interfaceClass);
            serviceProxyInstanceMap.put(interfaceName, instance);
        }
        return instance;
    }

    /**
     * 异步调用
     *
     * @param interfaceClass
     * @param method
     * @param parameters
     * @return
     * @throws Exception
     */
    public RpcFuture call(final Class<?> interfaceClass, Method method, Object[] parameters) throws RpcErrorException {
        // 获取 RPC 服务地址
        String interfaceClassName = interfaceClass.getName();
        String serviceAddress = this.findServiceAddress(interfaceClassName);
        //调用异步方法
        RpcFuture future;
        RpcClient client;
        try {
            // 创建 RPC 客户端对象并发送 RPC 请求
            client = this.getRpcClient(interfaceClassName, serviceAddress);
            future = client.invokeWithFuture(method, parameters);
        } catch (ConnectTimeoutException c) {
            try {
                //若旧服务地址已经更新，则更新本地缓存，否则抛出异常
                if (checkLogOut(interfaceClassName, serviceAddress)) {
                    throw new RpcErrorException(c.getMessage());
                }
                //更新地址，调用服务
                serviceAddress = this.findServiceAddress(interfaceClassName);
                client = this.getRpcClient(interfaceClassName, serviceAddress);
                future = client.invokeWithFuture(method, parameters);
            } catch (Exception e) {
                throw new RpcErrorException(e.getMessage());
            }
        } catch (Exception r) {
            throw new RpcErrorException(r.getMessage());
        }
        return future;
    }


    @SuppressWarnings("unchecked")
    private <T> T create(final Class<?> interfaceClass) {
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                (proxy, method, parameters) -> {
                    RpcResponse rpcResponse;
                    // 获取 RPC 服务地址
                    String interfaceClassName = interfaceClass.getName();
                    String serviceAddress = this.findServiceAddress(interfaceClassName);
                    // 创建 RPC 客户端对象并发送 RPC 请求
                    RpcClient client = this.getRpcClient(interfaceClassName, serviceAddress);
                    try {
                        //进行同步调用
                        rpcResponse = client.invokeSync(method, parameters);
                    } catch (ConnectTimeoutException e) {
                        //若旧服务地址已经更新，则更新本地缓存，否则抛出异常
                        if (checkLogOut(interfaceClassName, serviceAddress)) {
                            throw e;
                        }
                        //更新地址，调用服务
                        serviceAddress = this.findServiceAddress(interfaceClassName);
                        client = this.getRpcClient(interfaceClassName, serviceAddress);
                        rpcResponse = client.invokeSync(method, parameters);
                    } catch (Exception t) {
                        throw new RpcErrorException(t.getMessage());
                    }
                    // 返回 RPC 响应结果
                    if (null == rpcResponse || null == rpcResponse.getServiceResult()) {
                        throw new RpcErrorException(null == rpcResponse ? null : rpcResponse.getErrorMsg());
                    }
                    return rpcResponse.getServiceResult();
                }
        );
    }

    /**
     * 检查服务地址，若地址已注销则返回true,否则返回false,若地址被更新则同时更新本地缓存，
     *
     * @param interfaceClassName
     * @return
     * @throws Exception
     */
    private boolean checkLogOut(String interfaceClassName, String oldAddress) throws RpcErrorException, RpcServiceNotFoundException {
        if (serviceDiscovery == null) {
            throw new RpcErrorException("serviceDiscovery not exists.");
        }
        String newAddress = serviceDiscovery.discover(interfaceClassName);
        if (oldAddress.equals(newAddress)) {
            return false;
        }
        rpcClientMap.remove(oldAddress);
        if (newAddress == null) {
            return true;
        }
        this.getRpcClient(interfaceClassName, newAddress);
        return false;
    }

    /**
     * 获取已经初始化的RpcClient
     *
     * @param serviceAddress
     * @return
     */
    private RpcClient getRpcClient(String interfaceClassName, String serviceAddress) throws RpcServiceNotFoundException {
        if (StringUtil.isEmpty(serviceAddress)) {
            throw new RpcServiceNotFoundException();
        }
        RpcClient client = rpcClientMap.get(serviceAddress);
        //若不存在则创建和初始化，并进行缓存
        if (client == null) {
            // 从 RPC 服务地址中解析主机名与端口号
            String[] addressArray = StringUtil.split(serviceAddress, ":");
            String ip = addressArray[0];
            int port = Integer.parseInt(addressArray[1]);
            client = new RpcClient(ip, port, this.codeC);
            rpcClientMap.put(serviceAddress, client);
            addressCache.put(interfaceClassName, serviceAddress);
        }
        return client;
    }

    /**
     * 发现rpc服务地址，先从本地缓存找，找不到再查询zookeeper
     *
     * @param interfaceClassName
     * @return
     */
    private String findServiceAddress(String interfaceClassName) throws RpcErrorException {
        String serviceAddress = addressCache.get(interfaceClassName);
        if (serviceAddress != null) {
            return serviceAddress;
        }
        if (serviceDiscovery == null) {
            throw new RpcErrorException("serviceDiscovery not exists.");
        }
        try {
            serviceAddress = serviceDiscovery.discover(interfaceClassName);
        } catch (RpcServiceNotFoundException r) {
            LOGGER.debug("discover service: {}  is empty", interfaceClassName);
            return null;
        }
        LOGGER.info("discover service: {} => {}", interfaceClassName, serviceAddress);
        if (StringUtil.isEmpty(serviceAddress)) {
            throw new RpcErrorException(String.format("errorCode: %s, info: %s", RpcStateCode.SERVICE_NOT_EXISTS.getCode(), RpcStateCode.SERVICE_NOT_EXISTS.getValue()));
        }
        return serviceAddress;
    }
}
