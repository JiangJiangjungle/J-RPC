package com.jsj.nettyrpc.registry;

import com.jsj.nettyrpc.exception.RpcServiceNotFoundException;

/**
 * Service发现
 *
 * @author jsj
 * @date 2018-10-8
 */
public interface ServiceDiscovery {

    /**
     * 根据服务名称查找服务地址
     *
     * @param serviceName 服务名称
     * @return 服务地址
     */
    String discover(String serviceName) throws RpcServiceNotFoundException;
}