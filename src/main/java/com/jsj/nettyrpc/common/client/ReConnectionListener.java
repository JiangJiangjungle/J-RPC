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

    private Connection connection;

    public ReConnectionListener(Bootstrap bootstrap, String targetIP, int port, Connection connection) {
        this.bootstrap = bootstrap;
        this.targetIP = targetIP;
        this.port = port;
        this.connection = connection;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isSuccess()) {
            Channel channel = channelFuture.channel();
            LOGGER.debug("重连接成功: " + channel);
            //重新绑定channel
            connection.resetCount();
            connection.bind(channel);
        } else {
            connection.addRetryCount();
            Channel channel = channelFuture.channel();
            if (connection.getCount() < Connection.DEFAULT_RECONNECT_TRY) {
                ChannelPipeline channelPipeline = channel.pipeline();
                channelPipeline.fireChannelInactive();
            } else {
                LOGGER.debug("重连失败，且已经达到最大重试次数: " + Connection.DEFAULT_RECONNECT_TRY + ",不再进行重试!");
                connection.delete(channel);
            }
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

    public Connection getConnection() {
        return connection;
    }

}
