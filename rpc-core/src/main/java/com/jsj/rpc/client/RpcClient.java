package com.jsj.rpc.client;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.jsj.rpc.ChannelInfo;
import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.RpcException;
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
import io.netty.channel.ChannelFuture;
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
    protected final Endpoint endpoint;
    protected final RpcClientOptions clientOptions;
    protected NioEventLoopGroup workerGroup;
    protected Bootstrap bootstrap;
    protected Protocol protocol;
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
        Request request = protocol.createRequest();
        request.setRequestId(requestIdCounter.getAndIncrement());
        request.setServiceName(serviceInterface.getName());
        request.setMethod(method);
        request.setMethodName(method.getName());
        request.setParams(args);
        request.setCallback(callback);
        request.setWriteTimeoutMillis(clientOptions.getWriteTimeoutMillis());
        request.setReadTimeoutMillis(clientOptions.getReadTimeoutMillis());
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
    protected Channel selectChannel(Request request) throws Exception {
        return channelManager.selectChannel();
    }

    protected <T> RpcFuture<T> sendRequest(Request request) throws Exception {
        Channel channel = selectChannel(request);
        RpcFuture<T> rpcFuture = new RpcFuture<>(request);
        channel.eventLoop().submit(() -> {
            //在ChannelInfo中添加RpcFuture
            ChannelInfo channelInfo = ChannelInfo.getOrCreateClientChannelInfo(channel);
            channelInfo.addRpcFuture(rpcFuture);
        });
        //构造RequestMeta对象
        RpcMeta.RequestMeta.Builder metaBuilder = RpcMeta.RequestMeta.newBuilder();
        metaBuilder.setRequestId(request.getRequestId());
        metaBuilder.setServiceName(request.getServiceName());
        metaBuilder.setMethodName(request.getMethodName());
        for (Object param : request.getParams()) {
            if (param instanceof Message) {
                metaBuilder.addParams(Any.pack((Message) param));
            } else {
                throw new RuntimeException("param type must be Message!");
            }
        }
        RpcMeta.RequestMeta requestMeta = metaBuilder.build();
        //序列化并封装成Packet
        byte[] bytes = requestMeta.toByteArray();
        ByteBuf bodyBuf = Unpooled.buffer(bytes.length);
        bodyBuf.writeBytes(bytes);
        Packet packet = protocol.createPacket(bodyBuf);
        ChannelFuture channelFuture = channel.writeAndFlush(packet);
        boolean writeSucceed = channelFuture.awaitUninterruptibly(request.getReadTimeoutMillis());
        //已经写入完成，返还channel
        channelManager.returnChannel(channel);
        if (writeSucceed) {
            log.debug("Send rpc request succeed, request: {}.", requestMeta);
        } else {
            throw new RpcException(String.format("Send rpc request failed, request: %s.", requestMeta));
        }
        return rpcFuture;
    }

    public void shutdown() {
        //关闭所有连接
        channelManager.closeAll();
        if (!clientOptions.isGlobalThreadPoolSharing()) {
            //优雅退出，释放 NIO 线程组
            workerGroup.shutdownGracefully().awaitUninterruptibly();
            //释放业务线程池
            workerThreadPool.shutdown();
        }
        log.info("rpc client shutdown.");
    }
}
