package com.jsj.nettyrpc.common.client;

import io.netty.channel.Channel;

import java.util.Random;

/**
 * @author jsj
 * @date 2018-10-24
 */
public class ConnectionPool {

    private Channel[] channels;
    private int cap;
    private int size;
    private Random random = new Random();


    public ConnectionPool(int cap) {
        if (cap <= 0) {
            throw new IllegalArgumentException();
        }
        this.cap = cap;
        this.channels = new Channel[cap];
        this.size = 0;
    }

    public Channel get() {
        int index = random.nextInt(size);
        return channels[index];
    }

    public boolean add(Channel channel) {
        if (size < cap) {
            this.channels[size] = channel;
            size++;
        }
        return size < cap;
    }

    public void delete(Channel channel) {
        Channel now;
        for (int i = 0; i < size; i++) {
            now = channels[i];
            if (now == channel) {
                channels[i] = channels[size - 1];
                channels[size - 1] = null;
                size--;
            }
        }
    }

    public int getSize() {
        return size;
    }
}
