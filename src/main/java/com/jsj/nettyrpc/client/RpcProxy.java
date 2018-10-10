package com.jsj.nettyrpc.client;


import com.jsj.nettyrpc.common.RpcRequest;
import com.jsj.nettyrpc.common.RpcResponse;
import com.jsj.nettyrpc.common.RpcStateCode;
import com.jsj.nettyrpc.util.StringUtil;
import com.jsj.nettyrpc.registry.ServiceDiscovery;
import net.sf.cglib.proxy.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * RPC 代理（用于创建 RPC 服务代理）
 *
 * @author huangyong
 * @since 1.0.0
 */
public class RpcProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private String serviceAddress;

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

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
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, parameters) -> {
                    // 创建 RPC 请求对象并设置请求属性
                    RpcRequest request = this.buildRpcRequest(method, serviceVersion, parameters);
                    // 获取 RPC 服务地址
                    this.findServiceAddress(interfaceClass, serviceVersion);
                    if (StringUtil.isEmpty(serviceAddress)) {
                        throw new RuntimeException("server address is empty");
                    }
                    // 从 RPC 服务地址中解析主机名与端口号
                    String[] array = StringUtil.split(serviceAddress, ":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    // 创建 RPC 客户端对象并发送 RPC 请求
                    //todo 可以考虑改进，不要每次调用都创建一个连接
                    RpcClient client = new RpcClient(host, port);
                    long time = System.currentTimeMillis();
                    RpcResponse response = client.send(request);
                    LOGGER.debug("time: {}ms", System.currentTimeMillis() - time);
                    // 返回 RPC 响应结果
                    if (response == null || RpcStateCode.FAIL.equals(response.getRpcStateCode())) {
                        throw new RuntimeException(RpcStateCode.FAIL.getValue());
                    }
                    return response.getServiceResult();
                }
        );
    }

    /**
     * 创建并初始化 RPC 请求对象
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
        if (serviceDiscovery != null) {
            String serviceName = interfaceClass.getName();
            if (StringUtil.isNotEmpty(serviceVersion)) {
                serviceName += "-" + serviceVersion;
            }
            serviceAddress = serviceDiscovery.discover(serviceName);
            LOGGER.debug("discover service: {} => {}", serviceName, serviceAddress);
        }
        return serviceAddress;
    }
}
