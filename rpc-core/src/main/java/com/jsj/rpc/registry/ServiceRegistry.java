package com.jsj.rpc.registry;

/**
 * 服务注册中心
 *
 * @author jsj
 * @date 2018-10-8
 */
public interface ServiceRegistry {

    /**
     * 服务注册
     *
     * @param serviceName
     * @param ip
     * @param port
     */
    void register(String serviceName, String ip, int port) throws Exception;
}
