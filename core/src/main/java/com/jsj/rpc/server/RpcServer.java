package com.jsj.rpc.server;

/**
 * rpc服务端
 *
 * @author jiangshenjie
 */
public interface RpcServer {

    /**
     * rpc服务端启动
     */
    void start();

    /**
     * 注册服务实例
     *
     * @param serviceName
     * @param serviceBean
     * @return
     */
    boolean registerService(String serviceName, Object serviceBean);
}
