package com.jsj.rpc.client;


import com.jsj.rpc.RpcStateCode;
import com.jsj.rpc.codec.CodeC;
import com.jsj.rpc.codec.CodeStrategy;
import com.jsj.rpc.codec.DefaultCodeC;
import com.jsj.rpc.common.RpcFuture;
import com.jsj.rpc.common.RpcRequest;
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
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

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
    public static int RPC_TIMEOUT = 10000;
    public static int CONNECTION_READ_IDLE = 20000;
    public static int CONNECTION_WRITE_IDLE = 10000;

    private static int MAP_CAPACITY = 1 << 10;
    private static float LOAD_FACTOR = 0.95f;
    public static Map<Integer, RpcFuture> futureMap = new ConcurrentHashMap<>(MAP_CAPACITY, LOAD_FACTOR);

    private AtomicInteger requestId = new AtomicInteger(0);

    private ServiceDiscovery serviceDiscovery;
    /**
     * rpc service代理对象列表(用于客户端的同步调用),避免重复创建
     */
    private ConcurrentHashMap<String, Object> serviceProxyInstanceMap = new ConcurrentHashMap<>();

    /**
     * rpc服务的远程地址的本地缓存，减少查询zookeeper
     */
    private ConcurrentHashMap<String, String> addressCache = new ConcurrentHashMap<>();

    /**
     * rpc服务 ip:port->本地 rpcClient 映射列表,避免重复创建
     */
    private ConcurrentHashMap<String, RpcClient> rpcClientMap = new ConcurrentHashMap<>();

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
        // 创建 RPC 请求对象并设置请求属性
        RpcRequest request = this.buildRpcRequest(method, parameters);
        // 获取 RPC 服务地址
        String interfaceClassName = interfaceClass.getName();
        String serviceAddress = this.findServiceAddress(interfaceClassName);
        //调用异步方法
        RpcFuture future;
        RpcClient client;
        try {
            // 创建 RPC 客户端对象并发送 RPC 请求
            client = this.getRpcClient(interfaceClassName, serviceAddress);
            LOGGER.info("{}, rpc request:{}, 异步调用", LocalTime.now(), request);
            future = client.invokeWithFuture(request);
        } catch (ConnectTimeoutException c) {
            try {
                //若旧服务地址已经更新，则更新本地缓存，否则抛出异常
                if (checkLogOut(interfaceClassName, serviceAddress)) {
                    throw new RpcErrorException(c.getMessage());
                }
                //更新地址，调用服务
                serviceAddress = this.findServiceAddress(interfaceClassName);
                client = this.getRpcClient(interfaceClassName, serviceAddress);
                future = client.invokeWithFuture(request);
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
                    // 创建 RPC 请求对象并设置请求属性
                    RpcRequest request = this.buildRpcRequest(method, parameters);
                    // 获取 RPC 服务地址
                    String interfaceClassName = interfaceClass.getName();
                    String serviceAddress = this.findServiceAddress(interfaceClassName);
                    // 创建 RPC 客户端对象并发送 RPC 请求
                    RpcClient client = this.getRpcClient(interfaceClassName, serviceAddress);
                    LOGGER.info("rpc request:{}, 同步调用", request.toString());
                    try {
                        //加入超时处理
                        RpcFuture future = client.invokeWithFuture(request);
                        rpcResponse = future.get(RPC_TIMEOUT, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException t) {
                        //调用超时
                        futureMap.remove(request.getRequestId());
                        LOGGER.info("rpc request:{}, 调用超时", request.toString());
                        throw new RpcErrorException("调用超时，request：" + request.toString());
                    } catch (ConnectTimeoutException e) {
                        //若旧服务地址已经更新，则更新本地缓存，否则抛出异常
                        if (checkLogOut(interfaceClassName, serviceAddress)) {
                            throw e;
                        }
                        //更新地址，调用服务
                        serviceAddress = this.findServiceAddress(interfaceClassName);
                        client = this.getRpcClient(interfaceClassName, serviceAddress);
                        rpcResponse = client.invokeSync(request);
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

    /**
     * 创建并初始化 RpcRequest
     *
     * @param method
     * @param parameters
     * @return
     */
    private RpcRequest buildRpcRequest(Method method, Object[] parameters) {
        RpcRequest request = new RpcRequest();
        //若当前计数器值超过阈值，需要重置
        if (requestId.get() >= MAP_CAPACITY * LOAD_FACTOR) {
            synchronized (this) {
                if (requestId.get() >= MAP_CAPACITY * LOAD_FACTOR) {
                    requestId.getAndSet(0);
                }
            }
        }
        //设置请求id
        int id = requestId.getAndIncrement();
        request.setRequestId(id);
        //设置服务接口名称
        request.setInterfaceName(method.getDeclaringClass().getName());
        //设置调用方法名
        request.setMethodName(method.getName());
        //设置参数类型
        request.setParameterTypes(method.getParameterTypes());
        //设置参数值
        request.setParameters(parameters);
        return request;
    }
}
