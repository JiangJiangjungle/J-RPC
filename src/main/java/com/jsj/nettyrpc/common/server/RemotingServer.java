package com.jsj.nettyrpc.common.server;

/**
 * @author jsj
 * @date 2018-10-12
 */
public interface RemotingServer {
    /**
     * Get the ip of the server.
     *
     * @return ip
     */
    String ip();

    /**
     * Get the port of the server.
     *
     * @return listened port
     */
    int port();

    /**
     * 注册到服务中心
     */
    void registerAllService();
}
