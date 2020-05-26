package com.jsj.rpc.client;

import com.jsj.rpc.BasicSocketChannelInitializer;
import com.jsj.rpc.ChannelInfo;
import com.jsj.rpc.NamedThreadFactory;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.ProtocolManager;
import com.jsj.rpc.protocol.RequestMeta;
import com.jsj.rpc.protocol.RpcRequest;
import com.jsj.rpc.registry.ServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangshenjie
 */
@Slf4j
@Getter
public class RpcClient {
    private final RpcClientOptions clientOptions;
    private NioEventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private Protocol protocol;
    /**
     * 业务线程池
     */
    private ThreadPoolExecutor workerThreadPool;
    /**
     * 本地服务实例缓存
     */
    private ServiceInstanceManager serviceInstanceManager;
    /**
     * 服务发现
     */
    private ServiceDiscovery serviceDiscovery;

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        this(new RpcClientOptions(), serviceDiscovery);
    }

    public RpcClient(RpcClientOptions clientOptions, ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        this.clientOptions = clientOptions;
    }

    public void init() {
        serviceInstanceManager = new ServiceInstanceManager();
        protocol = ProtocolManager.getInstance().getProtocol(clientOptions.getProtocolType());
        workerGroup = new NioEventLoopGroup(clientOptions.getIoThreadNumber()
                , new NamedThreadFactory("rpc-client-io-thread", false));
        workerThreadPool = new ThreadPoolExecutor(clientOptions.getWorkerThreadNumber()
                , clientOptions.getWorkerThreadNumber(), 0L, TimeUnit.MILLISECONDS
                , new LinkedBlockingDeque<>(clientOptions.getWorkerThreadPoolQueueSize())
                , new NamedThreadFactory("rpc-client-worker-thread", false));
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
                .handler(new BasicSocketChannelInitializer(protocol, new RpcClientHandler(this)));
    }

    /**
     * 根据RpcRequest选取可用的Channel
     *
     * @param request
     * @return
     * @throws Exception
     */
    public Channel selectChannel(RpcRequest request) throws Exception {
        String serviceName = request.getServiceName();
        Channel channel = serviceInstanceManager.selectInstance(serviceName);
        //若本地缓存没有就从服务中心获取服务实例信息
        if (channel == null) {
            Endpoint endpoint = serviceDiscovery.discover(serviceName);
            if (endpoint == null) {
                throw new Exception(String.format("No available service: [%s]!", serviceName));
            }
            channel = createConnection(endpoint);
            serviceInstanceManager.addInstance(serviceName, channel);
        }
        return channel;
    }

    public <T> RpcFuture<T> sendRequest(RpcRequest request) throws Exception {
        Channel channel = selectChannel(request);
        DefaultRpcFuture<T> defaultRpcFuture = new DefaultRpcFuture<>(request);
        //必须在channel对应的eventLoop中获取ChannelInfo
        channel.eventLoop().submit(() -> {
            ChannelInfo channelInfo = ChannelInfo.getOrCreateClientChannelInfo(channel);
            channelInfo.addRpcFuture(defaultRpcFuture);
        });
        RequestMeta meta = new RequestMeta();
        meta.setRequestId(request.getRequestId());
        meta.setServiceName(request.getServiceName());
        meta.setMethodName(request.getMethodName());
        meta.setParams(request.getParams());
        channel.writeAndFlush(meta).addListener(ioFuture -> {
            if (!ioFuture.isSuccess()) {
                log.warn("Send rpc request failed, request id: {}.", meta.getRequestId());
            } else {
                log.info("Send rpc request succeed, request id: {}.", meta.getRequestId());
            }
        });
        return defaultRpcFuture;
    }

    public void shutdown() {
        //关闭所有连接
        serviceInstanceManager.close();
        //优雅退出，释放 NIO 线程组
        workerGroup.shutdownGracefully().awaitUninterruptibly();
        //释放业务线程池
        workerThreadPool.shutdown();
        log.info("rpc client shutdown.");
    }

    /**
     * 建立Channel连接
     *
     * @param endpoint
     * @return
     * @throws Exception
     */
    protected Channel createConnection(Endpoint endpoint) throws Exception {
        ChannelFuture future = bootstrap.connect(endpoint.getIp(), endpoint.getPort());
        //阻塞等待
        future.awaitUninterruptibly();
        future.sync();
        if (!future.isDone()) {
            String errMsg = String.format("Create connection to %s:%s timeout!", endpoint.getIp(), endpoint.getPort());
            log.warn(errMsg);
            throw new Exception(errMsg);
        }
        if (future.isCancelled()) {
            String errMsg = String.format("Create connection to %s:%s cancelled by user!", endpoint.getIp(), endpoint.getPort());
            log.warn(errMsg);
            throw new Exception(errMsg);
        }
        if (!future.isSuccess()) {
            String errMsg = String.format("Create connection to %s:%s error!", endpoint.getIp(), endpoint.getPort());
            log.warn(errMsg);
            throw new Exception(errMsg, future.cause());
        }
        log.info("Create connection to {}:{} success.", endpoint.getIp(), endpoint.getPort());
        return future.channel();
    }
}
