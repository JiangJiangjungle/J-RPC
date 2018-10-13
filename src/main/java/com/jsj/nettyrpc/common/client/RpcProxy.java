package com.jsj.nettyrpc.common.client;


import com.jsj.nettyrpc.common.RpcRequest;
import com.jsj.nettyrpc.common.RpcResponse;
import com.jsj.nettyrpc.common.RpcStateCode;
import com.jsj.nettyrpc.exception.RpcErrorException;
import com.jsj.nettyrpc.exception.RpcServiceNotFoundException;
import com.jsj.nettyrpc.util.StringUtil;
import com.jsj.nettyrpc.registry.ServiceDiscovery;
import net.sf.cglib.proxy.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC 代理（用于创建 RPC 服务代理）
 *
 * @author jsj
 * @date 2018-10-10
 */
public class RpcProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private ServiceDiscovery serviceDiscovery;

    /**
     * rpc service代理对象列表,避免重复创建
     */
    private ConcurrentHashMap<String, Object> serviceProxyInstanceMap = new ConcurrentHashMap<>();

    /**
     * RpcClient对象列表,避免重复创建
     */
    private ConcurrentHashMap<String, RpcClient> rpcClientMap = new ConcurrentHashMap<>();

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass) {
        return create(interfaceClass, "");
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                (proxy, method, parameters) -> {
                    RpcResponse rpcResponse;
                    // 创建 RPC 请求对象并设置请求属性
                    RpcRequest request = this.buildRpcRequest(method, serviceVersion, parameters);
                    // 获取 RPC 服务地址
                    String serviceAddress = this.findServiceAddress(interfaceClass, serviceVersion);
                    if (StringUtil.isEmpty(serviceAddress)) {
                        throw new RpcErrorException(String.format("errorCode: %s, info: %s", RpcStateCode.SERVICE_NOT_EXISTS.getCode(), RpcStateCode.SERVICE_NOT_EXISTS.getValue()));
                    }
                    // 创建 RPC 客户端对象并发送 RPC 请求
                    RpcClient client = this.getRpcClient(serviceAddress);
                    long time = System.currentTimeMillis();
                    rpcResponse = client.invokeSync(request);
                    System.out.println(rpcResponse);
                    LOGGER.debug("time: {}ms", System.currentTimeMillis() - time);
                    rpcResponse = client.invokeSync(request);
                    // 返回 RPC 响应结果
                    if (null == rpcResponse || null == rpcResponse.getServiceResult()) {
                        throw new RpcErrorException(null == rpcResponse ? null : rpcResponse.getErrorMsg());
                    }
                    return rpcResponse.getServiceResult();
                }
        );
    }

    /**
     * 获取已经初始化的RpcClient
     *
     * @param serviceAddress
     * @return
     */
    private RpcClient getRpcClient(String serviceAddress) {
        RpcClient client = rpcClientMap.get(serviceAddress);
        //若不存在则创建和初始化，并进行缓存
//        if (client == null) {
            // 从 RPC 服务地址中解析主机名与端口号
            String[] addressArray = StringUtil.split(serviceAddress, ":");
            String ip = addressArray[0];
            int port = Integer.parseInt(addressArray[1]);
            client = new RpcClient(ip, port);
            client.init();
            rpcClientMap.put(serviceAddress, client);
//        }
        return client;
    }

    /**
     * 获取RPC service的代理对象
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
     * 创建并初始化 RpcRequest
     *
     * @param method
     * @param serviceVersion
     * @param parameters
     * @return
     */
    private RpcRequest buildRpcRequest(Method method, String serviceVersion, Object[] parameters) {
        RpcRequest request = new RpcRequest();
        //设置请求id
        request.setRequestId(UUID.randomUUID().toString());
        //设置服务接口名称
        request.setInterfaceName(method.getDeclaringClass().getName());
        //设置服务版本号
        request.setServiceVersion(serviceVersion);
        //设置调用方法名
        request.setMethodName(method.getName());
        //设置参数类型
        request.setParameterTypes(method.getParameterTypes());
        //设置参数值
        request.setParameters(parameters);
        return request;
    }

    /**
     * 从服务中心发现rpc服务地址
     *
     * @param interfaceClass
     * @param serviceVersion
     * @return
     */
    private String findServiceAddress(Class<?> interfaceClass, String serviceVersion) {
        String serviceAddress = null;
        if (serviceDiscovery != null) {
            String serviceName = interfaceClass.getName();
            if (StringUtil.isNotEmpty(serviceVersion)) {
                serviceName += "-" + serviceVersion;
            }
            try {
                serviceAddress = serviceDiscovery.discover(serviceName);
            } catch (RpcServiceNotFoundException r) {
                LOGGER.debug("discover service: {}  is empty", serviceName);
                return null;
            }
            LOGGER.debug("discover service: {} => {}", serviceName, serviceAddress);
        }
        return serviceAddress;
    }
}
