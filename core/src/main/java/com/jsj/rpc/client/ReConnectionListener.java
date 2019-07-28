package com.jsj.rpc.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jsj
 * @date 2018-10-24
 */
public class ReConnectionListener implements ChannelFutureListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReConnectionListener.class);
    private Connection connection;

    public ReConnectionListener(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        Channel channel = channelFuture.channel();
        if (channelFuture.isSuccess()) {
            LOGGER.info("Reconnect success: {}", channel);
            //重新绑定channel
            connection.bind(channel);
        } else {
            connection.addRetryCount();
            if (connection.getCount() < connection.RECONNECT_TRY) {
                ChannelPipeline channelPipeline = channel.pipeline();
                channelPipeline.fireChannelInactive();
            } else {
                LOGGER.info("Fail to Reconnect. Retry:{}.", connection.RECONNECT_TRY);
                connection.unbind();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

}
