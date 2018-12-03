package com.jsj.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

/**
 * @author jsj
 * @date 2018-10-24
 */
public class Connection {

    private Channel channel;

    public static final int DEFAULT_RECONNECT_TRY = 20;
    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;

    private int count;

    private String targetIP;
    private int targetPort;
    private Bootstrap bootstrap;

    public Connection(String targetIP, int targetPort, Bootstrap bootstrap) {
        this.targetIP = targetIP;
        this.targetPort = targetPort;
        this.bootstrap = bootstrap;
    }

    public Connection() {
    }

    public Channel get() {
        return channel;
    }

    public void unbind() {
        this.count = 0;
        channel = null;
    }

    public void bind(Channel channel) {
        this.channel = channel;
        this.count = 0;
    }

    public void addRetryCount() {
        this.count += 1;
    }

    public int getCount() {
        return count;
    }

    public String getTargetIP() {
        return targetIP;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }
}
