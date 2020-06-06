package com.jsj.rpc;

import com.jsj.rpc.protocol.Protocol;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.HashMap;
import java.util.Map;


/**
 * Channel相关信息
 *
 * @author jiangshenjie
 */
public class ChannelInfo {
    private static final AttributeKey<ChannelInfo> RPC_SERVER_CHANNEL_INFO = AttributeKey.valueOf("rpc-server-channel-info");
    private static final AttributeKey<ChannelInfo> RPC_CLIENT_CHANNEL_INFO = AttributeKey.valueOf("rpc-client-channel-info");
    private Channel channel;
    private Protocol protocol;
    private Map<Long, RpcFuture<?>> rpcFutures = new HashMap<>(8);

    public static ChannelInfo getOrCreateClientChannelInfo(Channel channel) {
        if (channel == null) {
            throw new RuntimeException("channel cannot be null");
        }
        synchronized (channel) {
            Attribute<ChannelInfo> attribute = channel.attr(ChannelInfo.RPC_CLIENT_CHANNEL_INFO);
            ChannelInfo channelInfo = attribute.get();
            if (channelInfo == null) {
                channelInfo = new ChannelInfo();
                channelInfo.setChannel(channel);
                attribute.set(channelInfo);
            }
            return channelInfo;
        }
    }

    public static ChannelInfo getOrCreateServerChannelInfo(Channel channel) {
        if (channel == null) {
            throw new RuntimeException("channel cannot be null");
        }
        synchronized (channel) {
            Attribute<ChannelInfo> attribute = channel.attr(ChannelInfo.RPC_SERVER_CHANNEL_INFO);
            ChannelInfo channelInfo = attribute.get();
            if (channelInfo == null) {
                channelInfo = new ChannelInfo();
                channelInfo.setChannel(channel);
                attribute.set(channelInfo);
            }
            return channelInfo;
        }
    }

    public RpcFuture<?> getAndRemoveRpcFuture(long requestId) {
        return rpcFutures.remove(requestId);
    }

    public RpcFuture<?> getRpcFuture(long requestId) {
        return rpcFutures.get(requestId);
    }

    public void addRpcFuture(RpcFuture<?> rpcFuture) {
        rpcFutures.put(rpcFuture.getRequest().getRequestId(), rpcFuture);
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Map<Long, RpcFuture<?>> getRpcFutures() {
        return rpcFutures;
    }

    public void setRpcFutures(Map<Long, RpcFuture<?>> rpcFutures) {
        this.rpcFutures = rpcFutures;
    }
}
