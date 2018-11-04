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

    private int count = 0;


    public Connection() {
    }

    public Channel get() {
        return channel;
    }

    public void delete(Channel toDelete) {
        if (toDelete == channel) {
            channel = null;
        }
    }

    public void bind(Channel channel) {
        this.channel = channel;
    }

    public void addRetryCount() {
        this.count += 1;
    }

    public void resetCount() {
        this.count = 0;
    }

    public int getCount() {
        return count;
    }
}
