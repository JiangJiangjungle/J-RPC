package com.jsj.rpc;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class ChannelData {
    public static int FUTURE_MAP_INIT_SIZE = 32;

    private Channel channel;
    private long lastReceive;
    private Map<Integer, RpcFuture> rpcFutureMap = new HashMap<>(FUTURE_MAP_INIT_SIZE);

    public ChannelData(Channel channel, long lastReceive) {
        this.channel = channel;
        this.lastReceive = lastReceive;
    }

    public void update(long lastReceive) {
        this.lastReceive = lastReceive;
    }

    public RpcFuture getRpcFuture(Integer id) {
        return this.rpcFutureMap.get(id);
    }

    public RpcFuture removeRpcFuture(Integer id) {
        return this.rpcFutureMap.remove(id);
    }

    public void putRpcFuture(RpcFuture rpcFuture) {
        this.rpcFutureMap.put(rpcFuture.getRequestId(), rpcFuture);
    }

    public boolean expire(int channelAliveTime) {
        return System.currentTimeMillis() - lastReceive >= channelAliveTime;
    }
}
