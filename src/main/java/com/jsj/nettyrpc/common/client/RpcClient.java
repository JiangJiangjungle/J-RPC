package com.jsj.nettyrpc.common.client;


import com.jsj.nettyrpc.codec.RpcDecoder;
import com.jsj.nettyrpc.codec.RpcEncoder;
import com.jsj.nettyrpc.common.RpcFuture;
import com.jsj.nettyrpc.common.RpcRequest;
import com.jsj.nettyrpc.common.RpcResponse;
import com.jsj.nettyrpc.common.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * RPC 客户端（用于发送 RPC 请求）
 *
 * @author jsj
 * @date 2018-10-10
 */
public class RpcClient {
    private final String targetIP;
    private final int targetPort;

    public static final int DEFAULT_RECONNECT_TRY = 20;

    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;

    /**
     * writeAndFlush（）实际是提交一个task到EventLoopGroup，所以channel是可复用的
     */
    private ConnectionPool connectionPool = new ConnectionPool(1);

    private ConcurrentHashMap<Integer, RpcFuture> futureMap = new ConcurrentHashMap<>();
    private ChannelFactory connectionFactory;

    /**
     * 配置客户端 NIO 线程组
     */
    private EventLoopGroup group = new NioEventLoopGroup(1, new NamedThreadFactory(
            "Rpc-netty-client", false));
    /**
     * 创建并初始化 Netty 客户端 Bootstrap 对象
     */
    private Bootstrap bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true);

    public RpcClient(String targetIP, int targetPort) {
        this.targetIP = targetIP;
        this.targetPort = targetPort;
    }

    public void init() throws Exception {
        ReConnectionListener reconnectionListener = new ReConnectionListener(bootstrap, targetIP, targetPort, DEFAULT_RECONNECT_TRY, connectionPool);
        this.connectionFactory = new DefaultChannelFactory(this.bootstrap);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                //出方向编码
                pipeline.addLast(new IdleStateHandler(10, 5, 0, TimeUnit.SECONDS))
                        .addLast(new RpcEncoder(RpcRequest.class))
                        //入方向解码
                        .addLast(new RpcDecoder(RpcResponse.class))
                        .addLast(new ConnectionWatchDog(reconnectionListener))
                        //处理
                        .addLast(new ClientHandler(futureMap));
            }
        });
        Channel channel = this.connectionFactory.createConnection(this.targetIP, this.targetPort);
        connectionPool.add(channel);
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
        //注册到futureMap
        Integer requestId = request.getRequestId();
        RpcFuture future = new RpcFuture(requestId);
        this.futureMap.put(requestId, future);
        //发出请求，并直接返回
        this.getChannel().writeAndFlush(request);
        return future;
    }

    public Channel getChannel() throws Exception {
        Channel channel;
        if (connectionPool.getSize() == 0) {
            channel = this.connectionFactory.createConnection(this.targetIP, this.targetPort);
            connectionPool.add(channel);
            return channel;
        }
        channel = connectionPool.get();
        if (!channel.isActive()) {
            channel = this.connectionFactory.createConnection(this.targetIP, this.targetPort);
            connectionPool.add(channel);
            return channel;
        }
        return channel;
    }
}
