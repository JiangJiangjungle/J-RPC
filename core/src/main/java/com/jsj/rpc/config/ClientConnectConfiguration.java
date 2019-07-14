package com.jsj.rpc.config;

/**
 * 客户端连接配置
 *
 * @author jiangshenjie
 */
public class ClientConnectConfiguration {
    /**
     * 客户端断线重连的最大重试次数
     */
    private Integer maxRetries = 10;

    /**
     * 连接超时，单位：ms
     */
    private Integer connectTimeout = 2000;

    /**
     * 是否禁用NAGLE算法
     */
    private Boolean tcpNoDelay = true;

    /**
     * 异步调用超时，单位：ms
     */
    private Integer rpcRequestTimeout = 3000;

    public ClientConnectConfiguration() {
    }

    public ClientConnectConfiguration(Integer maxRetries, Integer connectTimeout, Boolean tcpNoDelay, Integer rpcRequestTimeout) {
        this.maxRetries = maxRetries;
        this.connectTimeout = connectTimeout;
        this.tcpNoDelay = tcpNoDelay;
        this.rpcRequestTimeout = rpcRequestTimeout;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Boolean getTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(Boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public Integer getRpcRequestTimeout() {
        return rpcRequestTimeout;
    }

    public void setRpcRequestTimeout(Integer rpcRequestTimeout) {
        this.rpcRequestTimeout = rpcRequestTimeout;
    }
}
