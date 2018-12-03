package com.jsj.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

public class HeartBeatConnectionHolder {

    public static final int DEFAULT_RECONNECT_TRY = 20;
    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;

    private int reConnectCount = 0;
    private final Bootstrap bootstrap;
    private final String targetIP;
    private final int port;

    private Channel channel;

    public HeartBeatConnectionHolder(Bootstrap bootstrap, String targetIP, int port) {
        this.bootstrap = bootstrap;
        this.targetIP = targetIP;
        this.port = port;
    }

    public void bind(Channel channel) {
        this.channel = channel;
        this.reConnectCount = 0;
    }

    public void unbind() {
        this.channel = null;
    }

    public boolean isActive() {
        return this.channel != null && this.channel.isActive();
    }

    public void addRetryCount() {
        this.reConnectCount += 1;
    }

    public int getCount() {
        return reConnectCount;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public String getTargetIP() {
        return targetIP;
    }

    public int getPort() {
        return port;
    }

    public Channel getChannel() {
        return channel;
    }
}
