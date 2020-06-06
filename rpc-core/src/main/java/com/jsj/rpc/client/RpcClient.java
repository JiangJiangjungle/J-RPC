package com.jsj.rpc.client;

import com.jsj.rpc.RpcCallback;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.codec.BaseDecoder;
import com.jsj.rpc.codec.BaseEncoder;
import com.jsj.rpc.protocol.ProtocolManager;
import com.jsj.rpc.protocol.Request;
import com.jsj.rpc.util.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jiangshenjie
 */
@Slf4j
@Getter
public class RpcClient extends AbstractRpcClient {
    protected final Endpoint endpoint;
    protected final RpcClientOptions clientOptions;
    protected Bootstrap bootstrap;

    protected NioEventLoopGroup workerGroup;
    protected ThreadPoolExecutor workerThreadPool;

    /**
     * 连接管理器
     */
    private ChannelManager channelManager;

    /**
     * 状态
     */
    private AtomicBoolean stop;
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

    public <T> Request buildRequest(Class<?> serviceInterface, String methodName
            , RpcCallback<T> callback, Object... args) throws Exception {
        Method method = getMethod(serviceInterface, methodName, args);
        return buildRequest(serviceInterface, method, callback, args);
    }

    public <T> Request buildRequest(Class<?> serviceInterface, Method method
            , RpcCallback<T> callback, Object... args) throws Exception {
        return protocol.createRequest()
                .setRequestId(requestIdCounter.getAndIncrement())
                .setServiceName(serviceInterface.getName())
                .setCallback(callback)
                .setMethod(method)
                .setMethodName(method.getName())
                .setWriteTimeoutMillis(clientOptions.getWriteTimeoutMillis())
                .setTaskTimeoutMills(clientOptions.getRpcTaskTimeoutMillis())
                .setParams(args);
    }

    public <T> RpcFuture<T> invoke(Class<?> serviceInterface, String methodName
            , RpcCallback<T> callback, Object... args) throws Exception {
        Request request = buildRequest(serviceInterface, methodName, callback, args);
        return sendRequest(request);
    }

    private Method getMethod(Class<?> serviceInterface, String methodName
            , Object... args) throws Exception {
        Class<?>[] argClasses = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argClasses[i] = args[i].getClass();
        }
        return serviceInterface.getDeclaredMethod(methodName, argClasses);
    }

    /**
     * 根据RpcRequest选取可用的Channel
     *
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    protected Channel selectChannel(Request request) throws Exception {
        return channelManager.borrowChannel();
    }

    @Override
    protected void processChannelAfterSendRequest(Channel channel) {
        channelManager.returnChannel(channel);
    }

    protected void init() {
        stop = new AtomicBoolean(false);
        requestIdCounter = new AtomicLong(0L);
        protocol = ProtocolManager.getInstance().getProtocol(clientOptions.getProtocolType());
        if (clientOptions.isGlobalThreadPoolSharing()) {
            workerGroup = ClientThreadPoolInstance.getOrCreateIoThreadPool(clientOptions.getIoThreadNumber());
            scheduledThreadPool = ClientThreadPoolInstance.getOrCreateScheduledThreadPool(clientOptions.getWorkerThreadNumber());
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
            scheduledThreadPool = new ScheduledThreadPoolExecutor(clientOptions.getWorkerThreadNumber()
                    , new NamedThreadFactory("rpc-client-scheduled-thread", false));
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
        //初始化ChannelManager
        channelManager = new ChannelManager(this);
    }

    public void shutdown() {
        if (stop.get()) {
            return;
        }
        stop.set(true);
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
