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
     * @param registerInfo
     * @throws Exception
     */
    void register(RegisterInfo registerInfo) throws Exception;
}
