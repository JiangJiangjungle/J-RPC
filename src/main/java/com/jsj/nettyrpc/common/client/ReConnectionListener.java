package com.jsj.nettyrpc.common.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jsj
 * @date 2018-10-24
 */
public class ReConnectionListener implements ChannelFutureListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReConnectionListener.class);

    private final Bootstrap bootstrap;
    private final String targetIP;
    private final int port;

    private final int RECONNECT_TRY;
    private final AtomicInteger count;

    private ConnectionPool connectionPool;

    public ReConnectionListener(Bootstrap bootstrap, String targetIP, int port, int RECONNECT_TRY, ConnectionPool connectionPool) {
        this.bootstrap = bootstrap;
        this.targetIP = targetIP;
        this.port = port;
        this.RECONNECT_TRY = RECONNECT_TRY;
        this.count = new AtomicInteger(0);
        this.connectionPool = connectionPool;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isSuccess()) {
            count.getAndSet(0);
            Channel channel = channelFuture.channel();
            LOGGER.debug("重连接成功: " + channel);
            connectionPool.add(channel);
        } else if (count.getAndIncrement() <= RECONNECT_TRY) {
            ChannelPipeline channelPipeline = channelFuture.channel().pipeline();
            channelPipeline.fireChannelInactive();
        } else {
            Channel channel = channelFuture.channel();
            LOGGER.debug("重连失败，且已经达到最大重试次数：" + RECONNECT_TRY + "，不再进行重试!");
            connectionPool.delete(channel);
        }
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

    public AtomicInteger getCount() {
        return count;
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

}
