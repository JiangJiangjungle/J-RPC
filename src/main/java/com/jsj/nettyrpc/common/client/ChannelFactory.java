package com.jsj.nettyrpc.common.client;


import io.netty.channel.Channel;

/**
 * @author jsj
 * @date 2018-10-24
 */
public interface ChannelFactory {

    /**
     * Create a connection according to the IP and port.
     * Note: The default protocol is RpcProtocol.
     *
     * @param targetIP       target ip
     * @param targetPort     target port
     * @param connectTimeout connect timeout in millisecond
     * @return connection
     */
    Channel createConnection(String targetIP, int targetPort, int connectTimeout) throws Exception;

    /**
     * Create a connection according to the IP and port.
     * Note: The default protocol is RpcProtocol.
     *
     * @param targetIP   target ip
     * @param targetPort target port
     * @return connection
     */
    Channel createConnection(String targetIP, int targetPort) throws Exception;

}
