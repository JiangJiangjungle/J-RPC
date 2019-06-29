package com.jsj.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

/**
 * @author jsj
 * @date 2018-10-24
 */
public class Connection {

    /**
     * 默认最大断线重连次数
     */
    public static final int DEFAULT_RECONNECT_TRY = 20;
    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;

    private volatile Channel channel;

    private int count;

    private String targetIP;
    private int targetPort;
    private Bootstrap bootstrap;
    public final int RECONNECT_TRY;
    public final int CONNECT_TIMEOUT;

    public Connection(String targetIP, int targetPort, Bootstrap bootstrap) {
        this.targetIP = targetIP;
        this.targetPort = targetPort;
        this.bootstrap = bootstrap;
        this.RECONNECT_TRY = DEFAULT_RECONNECT_TRY;
        this.CONNECT_TIMEOUT = DEFAULT_CONNECT_TIMEOUT;
    }

    public Connection(Channel channel, int count, String targetIP, int targetPort, Bootstrap bootstrap, int reconnect, int timeout) {
        this.channel = channel;
        this.count = count;
        this.targetIP = targetIP;
        this.targetPort = targetPort;
        this.bootstrap = bootstrap;
        this.RECONNECT_TRY = reconnect;
        this.CONNECT_TIMEOUT = timeout;
    }

    public void unbind() {
        this.count = 0;
        channel = null;
    }

    public void bind(Channel channel) {
        this.channel = channel;
        this.count = 0;
    }

    public Channel getChannel() {
        return channel;
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
