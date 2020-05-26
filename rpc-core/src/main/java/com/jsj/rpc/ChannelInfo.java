package com.jsj.rpc;

import com.jsj.rpc.client.DefaultRpcFuture;
import com.jsj.rpc.protocol.Protocol;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


/**
 * Channel相关信息：为保证线程安全的无锁操作，只能在channel所注册的eventloop中被调用
 *
 * @author jiangshenjie
 */
@Getter
@Setter
public class ChannelInfo {
    private static final AttributeKey<ChannelInfo> RPC_SERVER_CHANNEL_INFO = AttributeKey.valueOf("com.jsj.rpc-server-channel-info");
    private static final AttributeKey<ChannelInfo> RPC_CLIENT_CHANNEL_INFO = AttributeKey.valueOf("com.jsj.rpc-client-channel-info");
    private Channel channel;
    private Protocol protocol;
    private Map<Long, DefaultRpcFuture<?>> rpcFutures = new HashMap<>(8);

    public static ChannelInfo getOrCreateClientChannelInfo(Channel channel) {
        Attribute<ChannelInfo> attribute = channel.attr(ChannelInfo.RPC_CLIENT_CHANNEL_INFO);
        ChannelInfo channelInfo = attribute.get();
        if (channelInfo == null) {
            channelInfo = new ChannelInfo();
            channelInfo.setChannel(channel);
            attribute.set(channelInfo);
        }
        return channelInfo;
    }

    public static ChannelInfo getOrCreateServerChannelInfo(Channel channel) {
        Attribute<ChannelInfo> attribute = channel.attr(ChannelInfo.RPC_SERVER_CHANNEL_INFO);
        ChannelInfo channelInfo = attribute.get();
        if (channelInfo == null) {
            channelInfo = new ChannelInfo();
            channelInfo.setChannel(channel);
            attribute.set(channelInfo);
        }
        return channelInfo;
    }

    public DefaultRpcFuture<?> getAndRemoveRpcFuture(Long requestId) {
        return rpcFutures.remove(requestId);
    }

    public void addRpcFuture(DefaultRpcFuture<?> rpcFuture) {
        rpcFutures.put(rpcFuture.getRequest().getRequestId(), rpcFuture);
    }
}
