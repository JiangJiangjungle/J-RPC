package com.jsj.nettyrpc.common.client;


import com.jsj.nettyrpc.codec.CodeC;
import com.jsj.nettyrpc.codec.DefaultCodeC;
import com.jsj.nettyrpc.common.RpcRequest;
import com.jsj.nettyrpc.common.RpcResponse;
import com.jsj.nettyrpc.common.RpcFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

import java.util.concurrent.ConcurrentHashMap;


/**
 * RPC 客户端（用于发送 RPC 请求）
 *
 * @author jsj
 * @date 2018-10-10
 */
public class RpcClient {
    private final String targetIP;
    private final int targetPort;

    /**
     * writeAndFlush（）实际是提交一个task到EventLoopGroup，所以channel是可复用的
     */
    private Channel channel;

    private ConcurrentHashMap<Integer, RpcFuture> futureMap = new ConcurrentHashMap<>();
    private ChannelHandler channelHandler;
    private CodeC codeC;
    private ChannelFactory connectionFactory;

    public RpcClient(String targetIP, int targetPort) {
        this.targetIP = targetIP;
        this.targetPort = targetPort;
    }

    public void init() throws Exception {
        this.channelHandler = new ClientHandler(futureMap);
        this.codeC = new DefaultCodeC(RpcRequest.class, RpcResponse.class);
        this.connectionFactory = new DefaultChannelFactory(this.channelHandler, this.codeC);
        this.connectionFactory.init();
        this.channel = this.connectionFactory.createConnection(this.targetIP, this.targetPort);
    }

    /**
     * 同步调用
     *
     * @param request
     * @return
     * @throws Exception
     */
    public RpcResponse invokeSync(RpcRequest request) throws Exception {
        RpcFuture future = this.invokeWithFuture(request);
        return future.get();
    }

    /**
     * 异步调用
     *
     * @param request
     * @return
     * @throws Exception
     */
    public RpcFuture invokeWithFuture(RpcRequest request) throws Exception {
        this.checkChannel();
        //注册到futureMap
        Integer requestId = request.getRequestId();
        RpcFuture future = new RpcFuture(requestId);
        this.futureMap.put(requestId, future);
        //发出请求，并直接返回
        this.channel.writeAndFlush(request);
        return future;
    }

    /**
     * 关闭channel
     *
     * @throws Exception
     */
    public void shutdown() throws Exception {
        this.channel.close().sync();
    }

    /**
     * 检查channel 是否正常连接
     *
     * @throws Exception
     */
    private void checkChannel() throws Exception {
        if (!this.channel.isOpen()) {
            this.channel = this.connectionFactory.createConnection(this.targetIP, this.targetPort);
        }
    }
}
