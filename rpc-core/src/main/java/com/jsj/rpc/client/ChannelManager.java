package com.jsj.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Client的本地服务实例缓存
 *
 * @author jiangshenjie
 */
@Slf4j
@Getter
@Setter
public class ChannelManager {
    /**
     * Channel池
     */
    private GenericObjectPool<Channel> channelGenericObjectPool;
    private Endpoint endpoint;

    public ChannelManager(RpcClient rpcClient) {
        this.endpoint = rpcClient.getEndpoint();
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMinIdle(1);
        poolConfig.setMaxIdle(rpcClient.getClientOptions().getMaxChannelNumber());
        poolConfig.setMaxTotal(rpcClient.getClientOptions().getMaxChannelNumber());
        AbandonedConfig abandonedConfig = new AbandonedConfig();
        this.channelGenericObjectPool = new GenericObjectPool<>(
                new PooledChannelFactory(rpcClient), poolConfig, abandonedConfig);
    }

    public void closeChannel(Channel channel) throws Exception {
        channelGenericObjectPool.invalidateObject(channel);
    }


    /*******************以下方法只能由非EventLoop线程调用****************/

    /**
     * 返还channel
     *
     * @param channel
     * @return
     */
    public void returnChannel(Channel channel) {
        channelGenericObjectPool.returnObject(channel);
    }

    public void closeAll() {
        log.info("try to close all channels of rpc client: {}.", this.endpoint);
        channelGenericObjectPool.close();
    }

    /**
     * 获取一个服务节点对应的Channel
     *
     * @return
     */
    public Channel borrowChannel() throws Exception {
        return channelGenericObjectPool.borrowObject();
    }

    @Slf4j
    private static class PooledChannelFactory extends BasePooledObjectFactory<Channel> {
        private final Endpoint endpoint;
        private final Bootstrap bootstrap;
        private boolean enableHeartBeat;

        public PooledChannelFactory(RpcClient rpcClient) {
            this.endpoint = rpcClient.getEndpoint();
            this.bootstrap = rpcClient.getBootstrap();
        }

        @Override
        public Channel create() throws Exception {
            return createConnection();
        }

        @Override
        public PooledObject<Channel> wrap(Channel obj) {
            return new DefaultPooledObject<>(obj);
        }

        @Override
        public void destroyObject(PooledObject<Channel> p) throws Exception {
            Channel channel = p.getObject();
            if (channel != null && channel.isActive()) {
                channel.close();
                log.info("Channel {} closed proactively.", channel);
            }
        }

        @Override
        public boolean validateObject(PooledObject<Channel> p) {
            Channel channel = p.getObject();
            return channel != null && channel.isActive();
        }

        /**
         * 建立Channel连接
         *
         * @return
         * @throws Exception
         */
        protected Channel createConnection() throws Exception {
            ChannelFuture future = bootstrap.connect(endpoint.getIp(), endpoint.getPort());
            //阻塞等待
            future.awaitUninterruptibly();
            if (!future.isDone()) {
                String errMsg = String.format("Create connection to %s:%s timeout!", endpoint.getIp(), endpoint.getPort());
                log.warn(errMsg);
                throw new Exception(errMsg);
            }
            if (future.isCancelled()) {
                String errMsg = String.format("Create connection to %s:%s cancelled by user!", endpoint.getIp(), endpoint.getPort());
                log.warn(errMsg);
                throw new Exception(errMsg);
            }
            if (!future.isSuccess()) {
                String errMsg = String.format("Create connection to %s:%s error!", endpoint.getIp(), endpoint.getPort());
                log.warn(errMsg);
                throw new Exception(errMsg, future.cause());
            }
            Channel channel = future.channel();
            log.info("Created new connection: [local addr: {}, remote addr: {}].", channel.localAddress(), channel.remoteAddress());
            return channel;
        }
    }
}
