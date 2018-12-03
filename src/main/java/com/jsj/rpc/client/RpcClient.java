package com.jsj.rpc.client;


import com.jsj.rpc.codec.*;
import com.jsj.rpc.NamedThreadFactory;
import com.jsj.rpc.common.RpcFuture;
import com.jsj.rpc.common.RpcRequest;
import com.jsj.rpc.common.RpcResponse;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
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

    private final String targetIP;
    private final int targetPort;

    /**
     * writeAndFlush（）实际是提交一个task到EventLoopGroup，所以channel是可复用的
     */
    private Connection connection = new Connection();

    private static ChannelHandler clientHandler = new ClientHandler(RpcProxy.futureMap);

    /**
     * 配置客户端 NIO 线程组
     */
    private static EventLoopGroup group = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new NamedThreadFactory(
            "Rpc-netty-client", false));
    /**
     * 创建并初始化 Netty 客户端 Bootstrap 对象
     */
    private static Bootstrap bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
            //禁用nagle算法
            .option(ChannelOption.TCP_NODELAY, true);

    /**
     * 编解码方案
     *
     * @param targetIP
     * @param targetPort
     */
    private CodeC codeC;

    public RpcClient(String targetIP, int targetPort, CodeC codeC) {
        this.targetIP = targetIP;
        this.targetPort = targetPort;
        this.codeC = codeC;
        this.init();
    }

    private void init() {
        connection = new Connection(targetIP,targetPort,bootstrap);
        ReConnectionListener reConnectionListener = new ReConnectionListener(connection);
        RpcClient.bootstrap.handler(new ClientChannelInitializer(this.codeC, reConnectionListener, clientHandler));
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
        if (RpcProxy.futureMap.containsKey(requestId)) {
            throw new Exception("requestId 重复！！");
        }
        try {
            RpcProxy.futureMap.put(requestId, future);
            //发出请求，并直接返回
            this.getChannel().writeAndFlush(request);
        } catch (Exception e) {
            RpcProxy.futureMap.remove(requestId);
            throw e;
        }
        return future;
    }

    public Channel getChannel() throws Exception {
        Channel channel = connection.get();
        if (channel == null) {
            channel = doCreateConnection(this.targetIP, this.targetPort, Connection.DEFAULT_CONNECT_TIMEOUT);
            connection.bind(channel);
        }
        //重连失败则抛出异常
        if (!channel.isActive()) {
            throw new ConnectTimeoutException();
        }
        return channel;
    }

    /**
     * 借鉴 SOFA-BOLT 框架
     *
     * @param targetIP
     * @param targetPort
     * @param connectTimeout
     * @return
     * @throws Exception
     */
    private Channel doCreateConnection(String targetIP, int targetPort, int connectTimeout) throws Exception {
        // prevent unreasonable value, at least 1000
        connectTimeout = Math.max(connectTimeout, 1000);
        String address = targetIP + ":" + targetPort;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("connectTimeout of address [{}] is [{}].", address, connectTimeout);
        }
        RpcClient.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        //连接到远程节点，阻塞等待直到连接完成
        ChannelFuture future = bootstrap.connect(targetIP, targetPort);
        future.awaitUninterruptibly();
        if (!future.isDone()) {
            String errMsg = "Create connection to " + address + " timeout!";
            LOGGER.warn(errMsg);
            throw new Exception(errMsg);
        }
        if (future.isCancelled()) {
            String errMsg = "Create connection to " + address + " cancelled by user!";
            LOGGER.warn(errMsg);
            throw new Exception(errMsg);
        }
        if (!future.isSuccess()) {
            String errMsg = "Create connection to " + address + " error!";
            LOGGER.warn(errMsg);
            throw new Exception(errMsg, future.cause());
        }
        return future.channel();
    }
}
