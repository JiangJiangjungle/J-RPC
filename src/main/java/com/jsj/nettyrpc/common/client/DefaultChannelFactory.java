package com.jsj.nettyrpc.common.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 以工厂模式创建channel
 *
 * @author jsj
 * @date 2018-10-13
 */
public class DefaultChannelFactory implements ChannelFactory {


    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultChannelFactory.class);


    private Bootstrap bootstrap;

    public DefaultChannelFactory(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public Channel createConnection(String targetIP, int targetPort) throws Exception {
        return this.createConnection(targetIP, targetPort, RpcClient.DEFAULT_CONNECT_TIMEOUT);
    }

    @Override
    public Channel createConnection(String targetIP, int targetPort, int connectTimeout) throws Exception {
        return doCreateConnection(targetIP, targetPort, connectTimeout);
    }

    /**
     * 借鉴 SOFA-BOLT 框架
     *
     * @param targetIP
     * @param targetPort
     * @param connectTimeout
     * @return
     * @throws Exception
     */
    protected Channel doCreateConnection(String targetIP, int targetPort, int connectTimeout) throws Exception {
        // prevent unreasonable value, at least 1000
        connectTimeout = Math.max(connectTimeout, 1000);
        String address = targetIP + ":" + targetPort;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("connectTimeout of address [{}] is [{}].", address, connectTimeout);
        }
        this.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        //连接到远程节点，阻塞等待直到连接完成
        ChannelFuture future = bootstrap.connect(targetIP, targetPort);
        future.awaitUninterruptibly();
        if (!future.isDone()) {
            String errMsg = "Create connection to " + address + " timeout!";
            LOGGER.warn(errMsg);
            throw new Exception(errMsg);
        }
        if (future.isCancelled()) {
            String errMsg = "Create connection to " + address + " cancelled by user!";
            LOGGER.warn(errMsg);
            throw new Exception(errMsg);
        }
        if (!future.isSuccess()) {
            String errMsg = "Create connection to " + address + " error!";
            LOGGER.warn(errMsg);
            throw new Exception(errMsg, future.cause());
        }
        return future.channel();
    }
}
