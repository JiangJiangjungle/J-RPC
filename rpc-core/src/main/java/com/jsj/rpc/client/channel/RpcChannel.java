package com.jsj.rpc.client.channel;

import com.jsj.rpc.client.instance.Endpoint;
import io.netty.channel.Channel;

/**
 * @author jiangshenjie
 */
public interface RpcChannel {
    /**
     * 获取一个服务节点对应的Channel
     *
     * @return Channel
     */
    Channel getChannel() throws Exception;

    /**
     * 返还Channel
     *
     * @return Channel
     */
    void returnChannel(Channel channel);

    void removeChannel(Channel channel);

    Endpoint getEndpoint();

    void close();
}
