package com.jsj.rpc.client;

import com.jsj.rpc.ChannelInfo;
import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.codec.BaseDecoder;
import com.jsj.rpc.codec.BaseEncoder;
import com.jsj.rpc.protocol.*;
import com.jsj.rpc.util.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jiangshenjie
 */
@Slf4j
@Getter
public class RpcClient {
    private final Endpoint endpoint;
    private final RpcClientOptions clientOptions;
    private NioEventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private Protocol protocol;
    /**
     * 业务线程池
     */
    private ThreadPoolExecutor workerThreadPool;
    /**
     * 连接管理器
     */
    private ChannelManager channelManager;

    private AtomicLong requestIdCounter;

    public RpcClient(Endpoint endpoint) {
        this(endpoint, new RpcClientOptions());
    }

    public RpcClient(Endpoint endpoint, RpcClientOptions clientOptions) {
        this.endpoint = endpoint;
        this.clientOptions = clientOptions;
        this.init();
    }

    public static <T> T getProxy(RpcClient rpcClient, Class<T> clazz) {
        return RpcProxy.getProxy(rpcClient, clazz);
    }

    public <T> RpcFuture<T> invoke(Class<?> serviceInterface, Method method
            , RpcCallback<T> callback, Object... args) throws Exception {
        RpcRequest request = new RpcRequest();
        request.setRequestId(requestIdCounter.getAndIncrement());
        request.setServiceName(serviceInterface.getName());
        request.setMethod(method);
        request.setMethodName(method.getName());
        request.setParams(args);
        request.setCallback(callback);
        return sendRequest(request);
    }

    public <T> RpcFuture<T> invoke(Class<?> serviceInterface, String methodName
            , RpcCallback<T> callback, Object... args) throws Exception {
        Class<?>[] argClasses = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argClasses[i] = args[i].getClass();
        }
        Method method = serviceInterface.getDeclaredMethod(methodName, argClasses);
        return invoke(serviceInterface, method, callback, args);
    }

    protected void init() {
        requestIdCounter = new AtomicLong(0L);
        channelManager = new ChannelManager(this);
        protocol = ProtocolManager.getInstance().getProtocol(clientOptions.getProtocolType());
        if (clientOptions.isGlobalThreadPoolSharing()) {
            workerGroup = ClientThreadPoolInstance.getOrCreateIoThreadPool(clientOptions.getIoThreadNumber());
            workerThreadPool = ClientThreadPoolInstance.getOrCreateWorkThreadPool(
                    clientOptions.getWorkerThreadNumber()
                    , clientOptions.getWorkerThreadPoolQueueSize());
        } else {
            workerGroup = new NioEventLoopGroup(clientOptions.getIoThreadNumber()
                    , new NamedThreadFactory("rpc-client-io-thread", false));
            workerThreadPool = new ThreadPoolExecutor(clientOptions.getWorkerThreadNumber()
                    , clientOptions.getWorkerThreadNumber(), 0L, TimeUnit.MILLISECONDS
                    , new LinkedBlockingDeque<>(clientOptions.getWorkerThreadPoolQueueSize())
                    , new NamedThreadFactory("rpc-client-work-thread", false));
        }
        final RpcClient rpcClient = this;
        // init netty bootstrap
        bootstrap = new Bootstrap()
                //NioEventGroup
                .group(workerGroup)
                //ChannelFactory
                .channel(NioSocketChannel.class)
                //options
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientOptions.getConnectTimeoutMillis())
                .option(ChannelOption.SO_BACKLOG, clientOptions.getBacklog())
                .option(ChannelOption.SO_KEEPALIVE, clientOptions.isKeepAlive())
                .option(ChannelOption.TCP_NODELAY, clientOptions.isTcpNoDelay())
                .option(ChannelOption.SO_REUSEADDR, clientOptions.isReuseAddr())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_LINGER, clientOptions.getSoLinger())
                .option(ChannelOption.SO_SNDBUF, clientOptions.getSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, clientOptions.getReceiveBufferSize())
                //ChannelHandlerInitializer
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                //出方向编码
                                .addLast(new BaseEncoder(protocol))
                                //入方向解码
                                .addLast(new BaseDecoder(protocol))
                                //业务处理
                                .addLast(new RpcClientHandler(rpcClient));
                    }
                });
    }

    /**
     * 根据RpcRequest选取可用的Channel
     *
     * @param request
     * @return
     * @throws Exception
     */
    protected Channel selectChannel(RpcRequest request) throws Exception {
        return channelManager.selectChannel();
    }

    protected <T> RpcFuture<T> sendRequest(RpcRequest request) throws Exception {
        Channel channel = selectChannel(request);
        DefaultRpcFuture<T> defaultRpcFuture = new DefaultRpcFuture<>(request);
        //必须在channel对应的eventLoop中获取ChannelInfo
        channel.eventLoop().submit(() -> {
            ChannelInfo channelInfo = ChannelInfo.getOrCreateClientChannelInfo(channel);
            channelInfo.addRpcFuture(defaultRpcFuture);
        });
        RpcMeta.RequestMeta meta = request.requestMeta();
        byte[] bytes = meta.toByteArray();
        ByteBuf byteBuf = Unpooled.buffer(bytes.length);
        byteBuf.writeBytes(bytes);
        RpcPacket packet = new RpcPacket(byteBuf);
        channel.writeAndFlush(packet).addListener(ioFuture -> {
            if (!ioFuture.isSuccess()) {
                log.warn("Send rpc request failed, request id: {}.", meta.getRequestId());
            } else {
                log.info("Send rpc request succeed, request id: {}.", meta.getRequestId());
            }
            //已经写入完成，返还channel
            channelManager.returnChannel(channel);
        });
        return defaultRpcFuture;
    }

    public void shutdown() {
        //关闭所有连接
        channelManager.close();
        if (!clientOptions.isGlobalThreadPoolSharing()) {
            //优雅退出，释放 NIO 线程组
            workerGroup.shutdownGracefully().awaitUninterruptibly();
            //释放业务线程池
            workerThreadPool.shutdown();
        }
        log.info("rpc client shutdown.");
    }
}
