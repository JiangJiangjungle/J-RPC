package com.jsj.nettyrpc.common.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class Connection {

    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

    private Channel channel;

    private String ip;

    private int port;

    private String poolKey;

    private AtomicBoolean closed = new AtomicBoolean(false);

    public Connection(Channel channel, String ip, int port) {
        this.channel = channel;
        this.ip = ip;
        this.port = port;
        this.poolKey = ip + ":" + port;
    }

    public void close() {
        try {
            if (this.getChannel() != null) {
                this.getChannel().close().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Close the connection to remote address={}, port={}", ip, port);
                        }
                    }

                });
            }
        } catch (Exception e) {
            LOGGER.warn("Exception caught when closing connection {}", e);
        }
    }
}
