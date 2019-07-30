package com.jsj.rpc.server;

import com.jsj.rpc.RpcService;
import com.jsj.rpc.common.NamedThreadFactory;
import com.jsj.rpc.common.config.DefaultServerConfiguration;
import com.jsj.rpc.registry.ServiceRegistry;
import com.jsj.rpc.server.handler.RpcServiceChannelHandler;
import com.jsj.rpc.server.handler.ServerChannelInitializer;
import com.jsj.rpc.util.StringUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * RPC server启动器
 *
 * @author jsj
 * @date 2018-10-8
 */
public class DefaultRpcServer implements RpcServer {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultRpcServer.class);
    private static final String SEPARATOR = ":";
    /**
     * Netty 的连接线程池
     */
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1, new NamedThreadFactory(
            "Rpc-netty-server-boss", false));
    /**
     * Netty 的IO线程池
     */
    private EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2,
            new NamedThreadFactory("Rpc-netty-server-worker", true));

    /**
     * 业务处理器
     */
    private TaskExecutor taskExecutor = new DefaultTaskExecutor();

    /**
     * 服务端连接配置项
     */
    private DefaultServerConfiguration configuration;

    private String ip;

    private int port;

    /**
     * 服务注册中心
     */
    private ServiceRegistry serviceRegistry;

    public DefaultRpcServer(String ip, int port, ServiceRegistry serviceRegistry) {
        this(ip, port, serviceRegistry, new DefaultServerConfiguration());
    }

    public DefaultRpcServer(String ip, int port, ServiceRegistry serviceRegistry, DefaultServerConfiguration configuration) {
        this.ip = ip;
        this.port = port;
        this.serviceRegistry = serviceRegistry;
        this.configuration = configuration;
    }

    @Override
    public void start() {
        //启动
        doRunServer();
    }

    @Override
    public boolean registerService(String serviceName, Object serviceBean) {
        if (StringUtil.isEmpty(serviceName) || serviceBean == null) {
            LOGGER.debug("需要注册的 service 为空!");
            return false;
        }
        RpcService rpcService;
        String addr = this.ip + SEPARATOR + port;
        rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
        //service接口名称
        serviceName = rpcService.value().getName();
        //注册
        this.serviceRegistry.register(serviceName, addr);
        //添加到TaskExecutor
        this.taskExecutor.addInstance(serviceName, serviceBean);
        LOGGER.debug("register service: {} , addr: {}", serviceName, addr);
        return true;
    }

    /**
     * 启动 Netty RPC服务端
     */
    private void doRunServer() {
        new Thread(() -> {
            try {
                //创建并初始化 Netty 服务端辅助启动对象 ServerBootstrap
                ServerBootstrap serverBootstrap = DefaultRpcServer.this.initServerBootstrap(bossGroup, workerGroup);
                //绑定对应ip和端口，同步等待成功
                ChannelFuture future = serverBootstrap.bind(ip, port).sync();
                LOGGER.info("rpc server 已启动，端口：{}", port);
                //等待服务端监听端口关闭
                future.channel().closeFuture().sync();
            } catch (InterruptedException i) {
                LOGGER.error("rpc server 出现异常，端口：{}, cause: {}", port, i.getMessage());
            } finally {
                //优雅退出，释放 NIO 线程组
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }, "rpc-server-thread").start();
    }

    /**
     * 注册所有服务
     */
    protected void registerAllService(Map<String, Object> serviceBeanMap) {
        if (MapUtils.isEmpty(serviceBeanMap)) {
            LOGGER.debug("需要注册的 serviceMap 为空!");
        }
        for (Map.Entry<String, Object> entry : serviceBeanMap.entrySet()) {
            registerService(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 创建并初始化 Netty 服务端辅助启动对象 ServerBootstrap
     *
     * @param bossGroup
     * @param workerGroup
     * @return
     */
    private ServerBootstrap initServerBootstrap(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        return new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                //允许server端口reuse
                .option(ChannelOption.SO_REUSEADDR, this.configuration.getReUseAddr())
                .option(ChannelOption.SO_BACKLOG, this.configuration.getBackLog())
                .childOption(ChannelOption.SO_KEEPALIVE, this.configuration.getKeepAlive())
                .childHandler(new ServerChannelInitializer(this.configuration.getCodeC(),
                        new RpcServiceChannelHandler(this.taskExecutor), this.configuration.getChannelAliveTime()));
    }
}
