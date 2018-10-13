package com.jsj.nettyrpc.common.client;


import com.jsj.nettyrpc.codec.CodeC;
import com.jsj.nettyrpc.codec.DefaultCodeC;
import com.jsj.nettyrpc.common.RpcRequest;
import com.jsj.nettyrpc.common.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;


/**
 * RPC 客户端（用于发送 RPC 请求）
 *
 * @author jsj
 * @date 2018-10-10
 */
public class RpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private final String ip;
    private final int port;

    private ConcurrentHashMap<String, RpcResponse> responseMap = new ConcurrentHashMap<>();
    private ChannelHandler channelHandler;
    private CodeC codeC;
    private ConnectionFactory connectionFactory;

    public void init() {
        channelHandler = new ClientChannelHandler(responseMap);
        codeC = new DefaultCodeC(RpcRequest.class, RpcResponse.class);
        connectionFactory = new DefaultConnectionFactory(this.channelHandler, this.codeC);
        connectionFactory.init();
    }

    public RpcClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * 同步调用
     *
     * @param request
     * @return
     * @throws Exception
     */
    public RpcResponse invokeSync(RpcRequest request) throws Exception {
        Connection conn = connectionFactory.createConnection(ip, port);
        Channel channel = conn.getChannel();
        //阻塞直到
        channel.writeAndFlush(request).sync();
        // 保持阻塞直到channel关闭
        channel.closeFuture().sync();
        // 返回 RPC 响应对象
        return this.responseMap.remove(request.getRequestId());
    }
}
