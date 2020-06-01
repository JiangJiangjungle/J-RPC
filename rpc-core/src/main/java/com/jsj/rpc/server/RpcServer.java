package com.jsj.rpc.server;

import com.jsj.rpc.codec.BaseDecoder;
import com.jsj.rpc.codec.BaseEncoder;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.ProtocolManager;
import com.jsj.rpc.registry.ServiceRegistry;
import com.jsj.rpc.util.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangshenjie
 */
@Slf4j
@Getter
@Setter
public class RpcServer {
    private final String ip;
    private final int port;
    private final RpcServerOptions serverOptions;
    /**
     * Netty 的accept线程池
     */
    private EventLoopGroup bossGroup;
    /**
     * Netty 的io线程池
     */
    private EventLoopGroup workerGroup;
    /**
     * Netty server启动配置类
     */
    private ServerBootstrap serverBootstrap;
    /**
     * 业务线程池
     */
    private ThreadPoolExecutor workerThreadPool;
    /**
     * 协议类型
     */
    private Protocol protocol;
    /**
     * 服务注册
     */
    private ServiceRegistry serviceRegistry;

    public RpcServer(String ip, int port) {
        this(ip, port, new RpcServerOptions());
    }

    public RpcServer(String ip, int port, RpcServerOptions serverOptions) {
        this.ip = ip;
        this.port = port;
        this.serverOptions = serverOptions;
    }

    public void registerService(Object service) throws Exception {
        registerService(service, null);
    }

    public void registerService(Object service, Class<?> targetInterface) throws Exception {
        ServiceManager serviceManager = ServiceManager.getInstance();
        serviceManager.registerService(service, targetInterface);
        //注册到服务中心
        if (serviceRegistry != null) {
            serviceRegistry.register(targetInterface.getName(), ip, port);
        }
    }

    private void init() {
        protocol = ProtocolManager.getInstance().getProtocol(serverOptions.getProtocolType());
        bossGroup = new NioEventLoopGroup(serverOptions.getAcceptThreadNumber()
                , new NamedThreadFactory("rpc-server-accept-thread", false));
        workerGroup = new NioEventLoopGroup(serverOptions.getIoThreadNumber()
                , new NamedThreadFactory("rpc-server-io-thread", false));
        workerThreadPool = new ThreadPoolExecutor(serverOptions.getWorkerThreadNumber()
                , serverOptions.getWorkerThreadNumber(), 0L, TimeUnit.MILLISECONDS
                , new LinkedBlockingDeque<>(serverOptions.getWorkerThreadPoolQueueSize())
                , new NamedThreadFactory("rpc-server-worker-thread", false));
        final RpcServer rpcServer = this;
        serverBootstrap = new ServerBootstrap()
                //NioEventGroup
                .group(bossGroup, workerGroup)
                //ChannelFactory
                .channel(NioServerSocketChannel.class)
                //options
                .option(ChannelOption.SO_BACKLOG, serverOptions.getBacklog())
                .childOption(ChannelOption.SO_KEEPALIVE, serverOptions.isKeepAlive())
                .childOption(ChannelOption.TCP_NODELAY, serverOptions.isTcpNoDelay())
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_LINGER, serverOptions.getSoLinger())
                .childOption(ChannelOption.SO_SNDBUF, serverOptions.getSendBufferSize())
                .childOption(ChannelOption.SO_RCVBUF, serverOptions.getReceiveBufferSize())
                //ChannelHandlerInitializer
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                //出方向编码
                                .addLast(new BaseEncoder(protocol))
                                //入方向解码
                                .addLast(new BaseDecoder(protocol))
                                //业务处理
                                .addLast(new RpcServerHandler(rpcServer));
                    }
                });
    }

    public boolean start() {
        init();
        boolean success = false;
        try {
            //绑定对应ip和端口（阻塞等待）
            serverBootstrap.bind(ip, port).sync();
            success = true;
            log.info("rpc server started, listen port：{}.", port);
        } catch (InterruptedException i) {
            log.error("rpc server start failed, listen port：{}, msg: {}"
                    , port, i.getMessage(), i);
        }
        return success;
    }

    public void shutdown() {
        //优雅退出，释放 NIO 线程组
        workerGroup.shutdownGracefully().awaitUninterruptibly();
        bossGroup.shutdownGracefully().awaitUninterruptibly();
        //释放业务线程池
        workerThreadPool.shutdown();
        log.info("com.jsj.rpc server shutdown.");
    }
}
