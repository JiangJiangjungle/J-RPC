package com.jsj.nettyrpc.connection;

public interface ConnectionFactory {

    void init();

    /**
     * Create a connection according to the IP and port.
     * Note: The default protocol is RpcProtocol.
     *
     * @param targetIP       target ip
     * @param targetPort     target port
     * @param connectTimeout connect timeout in millisecond
     * @return connection
     */
    Connection createConnection(String targetIP, int targetPort, int connectTimeout) throws Exception;

}
