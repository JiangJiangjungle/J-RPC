package com.jsj.rpc.registry;

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
     * @param subscribeInfo
     * @return
     */
    ServiceInstance discover(SubscribeInfo subscribeInfo);
}