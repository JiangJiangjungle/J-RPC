package com.jsj.nettyrpc.common.client;


import com.jsj.nettyrpc.codec.*;
import com.jsj.nettyrpc.common.NamedThreadFactory;
import com.jsj.nettyrpc.common.RpcFuture;
import com.jsj.nettyrpc.common.RpcRequest;
import com.jsj.nettyrpc.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


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

    private ConcurrentHashMap<Integer, RpcFuture> futureMap = new ConcurrentHashMap<>();

    /**
     * 配置客户端 NIO 线程组
     */
    private EventLoopGroup group = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new NamedThreadFactory(
            "Rpc-netty-client", false));
    /**
     * 创建并初始化 Netty 客户端 Bootstrap 对象
     */
    private Bootstrap bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true);

    /**
     * 编解码方案
     *
     * @param targetIP
     * @param targetPort
     */
    private CodeC codeC;

    public RpcClient(String targetIP, int targetPort, CodeStrategy codeSrategy) {
        this.targetIP = targetIP;
        this.targetPort = targetPort;
        this.codeC = new DefaultCodeC(RpcRequest.class, RpcResponse.class, codeSrategy);
        this.init();
    }

    public RpcClient(String targetIP, int targetPort) {
        this(targetIP, targetPort, CodeStrategy.DEAULT);
    }

    public void init() {
        ReConnectionListener reConnectionListener = new ReConnectionListener(bootstrap, targetIP, targetPort, connection);
        bootstrap.handler(new ClientChannelInitializer(this.codeC, new ConnectionWatchDog(reConnectionListener), new ClientHandler(futureMap)));
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
        Channel channel = connection.get();
        if (channel == null) {
            channel = doCreateConnection(this.targetIP, this.targetPort, Connection.DEFAULT_CONNECT_TIMEOUT);
            connection.bind(channel);
        }
        while (!channel.isActive() && connection.getCount() < Connection.DEFAULT_RECONNECT_TRY) {
            channel = connection.get();
        }
        //重连失败则抛出异常
        if (!channel.isActive() && connection.getCount() == Connection.DEFAULT_RECONNECT_TRY) {
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
        this.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
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
