package com.jsj.rpc.registry;

/**
 * Service注册
 *
 * @author jsj
 * @date 2018-10-8
 */
public interface ServiceRegistry {

    /**
     * 注册服务名称与服务地址
     *
     * @param serviceName    服务名称
     * @param serviceAddress 服务地址
     */
    void register(String serviceName, String serviceAddress);
}
