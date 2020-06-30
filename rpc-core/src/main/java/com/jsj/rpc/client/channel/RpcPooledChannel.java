package com.jsj.rpc.client.channel;

import com.jsj.rpc.client.RpcClient;
import com.jsj.rpc.client.instance.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
@Setter
public class RpcPooledChannel implements RpcChannel {
    /**
     * Netty Channel池
     */
    private GenericObjectPool<Channel> channelPool;
    private Endpoint endpoint;

    public RpcPooledChannel(RpcClient rpcClient) {
        this.endpoint = rpcClient.getEndpoint();
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMinIdle(1);
        poolConfig.setMaxIdle(rpcClient.getClientOptions().getMaxChannelNumber());
        poolConfig.setMaxTotal(rpcClient.getClientOptions().getMaxChannelNumber());
        AbandonedConfig abandonedConfig = new AbandonedConfig();
        this.channelPool = new GenericObjectPool<>(
                new PooledChannelFactory(endpoint, rpcClient.getBootstrap())
                , poolConfig, abandonedConfig);
    }

    @Override
    public Channel getChannel() throws Exception {
        return channelPool.borrowObject();
    }

    @Override
    public void removeChannel(Channel channel) {
        try {
            channelPool.invalidateObject(channel);
        } catch (Exception e) {
            log.warn("Fail to remove channel in channel pool", e);
        }
    }

    @Override
    public Endpoint getEndpoint() {
        return this.endpoint;
    }

    @Override
    public void close() {
        log.debug("try to close all channels of rpc client: {}.", this.endpoint);
        channelPool.close();
    }

    /*******************以下方法只能由非EventLoop线程调用****************/
    @Override
    public void returnChannel(Channel channel) {
        channelPool.returnObject(channel);
    }

    @Slf4j
    private static class PooledChannelFactory extends BasePooledObjectFactory<Channel> {
        private final Endpoint endpoint;
        private final Bootstrap bootstrap;

        public PooledChannelFactory(Endpoint endpoint, Bootstrap bootstrap) {
            this.endpoint = endpoint;
            this.bootstrap = bootstrap;
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
        public void destroyObject(PooledObject<Channel> p) {
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
