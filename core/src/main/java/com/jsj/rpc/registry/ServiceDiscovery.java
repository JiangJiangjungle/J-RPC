package com.jsj.rpc.registry;

import com.jsj.rpc.exception.RpcServiceNotFoundException;

/**
 * 服务发现中心
 *
 * @author jsj
 * @date 2018-10-8
 */
public interface ServiceDiscovery {

    /**
     * 服务发现
     *
     * @param serviceName
     * @return
     * @throws RpcServiceNotFoundException
     */
    String discover(String serviceName) throws RpcServiceNotFoundException;
}