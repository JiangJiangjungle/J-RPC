package com.jsj.rpc;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * 每个channel保存在绑定的EventLoop线程的ThreadLocal数据
 *
 * @author jiangshenjie
 */
public class ChannelData {
    private static int FUTURE_MAP_INIT_SIZE = 32;

    private Channel channel;
    private long lastReceive;
    private Map<Integer, DefaultRpcFuture> rpcFutureMap = new HashMap<>(FUTURE_MAP_INIT_SIZE);

    public ChannelData(Channel channel, long lastReceive) {
        this.channel = channel;
        this.lastReceive = lastReceive;
    }

    public void update(long lastReceive) {
        this.lastReceive = lastReceive;
    }

    public DefaultRpcFuture getRpcFuture(Integer id) {
        return this.rpcFutureMap.get(id);
    }

    public DefaultRpcFuture removeRpcFuture(Integer id) {
        return this.rpcFutureMap.remove(id);
    }

    public void putRpcFuture(DefaultRpcFuture defaultRpcFuture) {
        this.rpcFutureMap.put(defaultRpcFuture.getRequestId(), defaultRpcFuture);
    }

    public boolean expire(int channelAliveTime) {
        return System.currentTimeMillis() - lastReceive >= channelAliveTime;
    }
}
