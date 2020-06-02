package com.jsj.rpc.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
     * 对端（服务端ip+port）信息
     */
    private final RpcClient rpcClient;
    private final Lock lock = new ReentrantLock();
    /**
     * 与每个Endpoint保持的最大连接数
     */
    private static int CHANNEL_NUMBER = 3;
    /**
     * 与对应服务端所保持的可用连接列表
     */
    private LinkedList<Channel> availableChannels = new LinkedList<>();
    /**
     * 被占用连接列表
     */
    private LinkedList<Channel> occupiedChannels = new LinkedList<>();

    public ChannelManager(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    /**
     * 获取服务节点对应的Channel
     *
     * @return
     */
    public Channel selectChannel() throws Exception {
        lock.lock();
        try {
            Channel channel;
            if (availableChannels.size() < CHANNEL_NUMBER) {
                channel = createConnection();
                availableChannels.addLast(channel);
            }
            channel = availableChannels.removeLast();
            occupiedChannels.addLast(channel);
            return channel;
        } finally {
            lock.unlock();
        }

    }

    /**
     * 返还channel
     *
     * @param channel
     * @return
     */
    public boolean returnChannel(Channel channel) {
        if (channel.isActive()) {
            lock.lock();
            try {
                occupiedChannels.remove(channel);
                availableChannels.addLast(channel);
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    /**
     * 移除Channel
     *
     * @param channel
     */
    public boolean removeChannel(Channel channel) {
        lock.lock();
        try {
            if (!availableChannels.remove(channel) && !occupiedChannels.remove(channel)) {
                return false;
            }
            if (!channel.isActive()) {
                return true;
            }
            channel.close().addListener(future -> {
                if (future.isSuccess()) {
                    log.info("Channel closed, remote addr: {},local addr: {}"
                            , rpcClient.getEndpoint().toString(), channel.localAddress().toString());
                }
            });
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 只能由非EventLoop线程调用！
     */
    public void close() {
        lock.lock();
        try {
            for (Channel each : availableChannels) {
                each.close().awaitUninterruptibly();
            }
            for (Channel each : occupiedChannels) {
                each.close().awaitUninterruptibly();
            }
        } finally {
            lock.unlock();
        }
        log.info("Close all channels.");
    }

    private Endpoint parseAddress(InetSocketAddress address) {
        String ip = address.getAddress().getHostAddress();
        int port = address.getPort();
        return new Endpoint(ip, port);
    }

    /**
     * 建立Channel连接
     *
     * @return
     * @throws Exception
     */
    protected Channel createConnection() throws Exception {
        Endpoint endpoint = rpcClient.getEndpoint();
        ChannelFuture future = rpcClient.getBootstrap().connect(endpoint.getIp(), endpoint.getPort());
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
        log.info("Create connection to {}:{} success.", endpoint.getIp(), endpoint.getPort());
        return future.channel();
    }

}
