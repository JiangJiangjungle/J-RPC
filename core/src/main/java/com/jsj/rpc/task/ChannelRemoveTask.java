package com.jsj.rpc.task;

import com.jsj.rpc.ChannelDataHolder;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;

import java.util.concurrent.TimeUnit;

/**
 * 定时删除无效channel，避免TCP半连接占用过多文件描述符,EventLoop线程执行
 */
public class ChannelRemoveTask implements Runnable {
    private Channel channel;
    private EventLoop eventLoop;
    private int channelAliveTime;

    public ChannelRemoveTask(Channel channel, EventLoop eventLoop, int channelAliveTime) {
        this.channel = channel;
        this.eventLoop = eventLoop;
        this.channelAliveTime = channelAliveTime;
    }

    @Override
    public void run() {
        if (ChannelDataHolder.removeIfExpire(channel, channelAliveTime)) return;
        this.eventLoop.schedule(this, channelAliveTime, TimeUnit.MILLISECONDS);
    }
}
