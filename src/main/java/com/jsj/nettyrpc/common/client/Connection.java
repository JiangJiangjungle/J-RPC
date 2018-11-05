package com.jsj.nettyrpc.common.client;

import io.netty.channel.Channel;

import java.util.Random;

/**
 * @author jsj
 * @date 2018-10-24
 */
public class Connection {

    private Channel channel;

    public static final int DEFAULT_RECONNECT_TRY = 20;
    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;

    private int count;


    public Connection() {
    }

    public Channel get() {
        return channel;
    }

    public void delete() {
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
}
