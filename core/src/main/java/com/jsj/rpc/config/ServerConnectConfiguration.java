package com.jsj.rpc.config;

/**
 * 服务端连接配置
 *
 * @author jiangshenjie
 */
public class ServerConnectConfiguration {
    /**
     * 未接收到心跳包后的TCP连接最大存活时间，单位：ms
     */
    private Integer channelAliveTime = 20000;

    private Integer backLog = 1024;

    /**
     * 是否使用keepAlive模式
     */
    private Boolean keepAlive = true;


    public ServerConnectConfiguration() {
    }

    public ServerConnectConfiguration(Integer channelAliveTime) {
        this.channelAliveTime = channelAliveTime;
    }

    public Integer getChannelAliveTime() {
        return channelAliveTime;
    }

    public Integer getBackLog() {
        return backLog;
    }

    public Boolean getKeepAlive() {
        return keepAlive;
    }
}
