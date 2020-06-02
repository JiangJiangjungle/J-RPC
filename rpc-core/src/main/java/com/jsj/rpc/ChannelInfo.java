package com.jsj.rpc;

import com.jsj.rpc.protocol.Protocol;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


/**
 * Channel相关信息
 *
 * @author jiangshenjie
 */
@Getter
@Setter
public class ChannelInfo {
    private static final AttributeKey<ChannelInfo> RPC_SERVER_CHANNEL_INFO = AttributeKey.valueOf("rpc-server-channel-info");
    private static final AttributeKey<ChannelInfo> RPC_CLIENT_CHANNEL_INFO = AttributeKey.valueOf("rpc-client-channel-info");
    private Channel channel;
    private Protocol protocol;
    private Map<Long, RpcFuture<?>> rpcFutures = new HashMap<>(8);

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

    public RpcFuture<?> getAndRemoveRpcFuture(Long requestId) {
        return rpcFutures.remove(requestId);
    }

    public RpcFuture<?> getRpcFuture(Long requestId) {
        return rpcFutures.get(requestId);
    }

    public void addRpcFuture(RpcFuture<?> rpcFuture) {
        rpcFutures.put(rpcFuture.getRequest().getRequestId(), rpcFuture);
    }
}
