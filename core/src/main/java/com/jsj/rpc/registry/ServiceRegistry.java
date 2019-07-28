package com.jsj.rpc.registry;

/**
 * Service服务中心
 *
 * @author jsj
 * @date 2018-10-8
 */
public interface ServiceRegistry {

    /**
     * 服务注册
     *
     * @param serviceName    服务名称
     * @param serviceAddress 服务地址
     */
    void register(String serviceName, String serviceAddress);
}
