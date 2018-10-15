package com.jsj.nettyrpc.common.server;

import com.jsj.nettyrpc.RpcService;
import com.jsj.nettyrpc.codec.RpcDecoder;
import com.jsj.nettyrpc.codec.RpcEncoder;
import com.jsj.nettyrpc.common.RpcRequest;
import com.jsj.nettyrpc.common.RpcResponse;
import com.jsj.nettyrpc.registry.ServiceRegistry;
import com.jsj.nettyrpc.util.StringUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RPC server
 *
 * @author jsj
 * @date 2018-10-8
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String ip;

    private int port;

    private String addr;

    /**
     * Netty 的连接线程池
     */
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1, new NamedThreadFactory(
            "Rpc-netty-server-boss", false));
    /**
     * Netty 的Task执行线程池
     */
    private EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2,
            new NamedThreadFactory("Rpc-netty-server-worker", true));

    /**
     * 服务注册中心
     */
    private ServiceRegistry serviceRegistry;

    /**
     * 用于存储已经注册的服务实例
     */
    private Map<String, Object> serviceInstanceMap = new HashMap<>();

    public RpcServer(String ip, int port, ServiceRegistry serviceRegistry) {
        this.ip = ip;
        this.port = port;
        this.addr = this.ip + ":" + this.port;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * 初始化bean初始化完成后调用：扫描带有 RpcService 注解的服务并添加到 handlerMap ，并进行服务注册
     *
     * @throws Exception
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //扫描带有 RpcService 注解的类并初始化 handlerMap 对象
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        //注册
        this.registerAllService(serviceBeanMap);
    }

    /**
     * 初始化bean时调用：启动 Netty RPC服务器服务端
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            //创建并初始化 Netty 服务端辅助启动对象 ServerBootstrap
            ServerBootstrap serverBootstrap = this.initServerBootstrap(this.bossGroup, this.workerGroup);
            //绑定对应ip和端口，同步等待成功
            ChannelFuture future = serverBootstrap.bind(ip, port).sync();
            LOGGER.debug("server started on port {}", port);
            //等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } finally {
            //优雅退出，释放 NIO 线程组
            this.workerGroup.shutdownGracefully();
            this.bossGroup.shutdownGracefully();
        }
    }

    /**
     * 注册所有服务
     */
    private void registerAllService(Map<String, Object> serviceBeanMap) {
        if (MapUtils.isEmpty(serviceBeanMap)) {
            return;
        }
        RpcService rpcService;
        String serviceName;
        Object serviceBean;
        for (Map.Entry<String, Object> entry : serviceBeanMap.entrySet()) {
            //service实例
            serviceBean = entry.getValue();
            rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
            //service接口名称
            serviceName = rpcService.value().getName();
            //注册
            this.serviceRegistry.register(serviceName, this.addr);
            this.serviceInstanceMap.put(serviceName, serviceBean);
            LOGGER.debug("register service: {} => {}", serviceName, this.addr);
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
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                // 解码 RPC 请求
                                .addLast(new RpcDecoder(RpcRequest.class))
                                // 编码 RPC 响应
                                .addLast(new RpcEncoder(RpcResponse.class))
                                // 处理 RPC 请求
                                .addLast(new RpcServiceHandler(serviceInstanceMap));
                    }
                });
    }

    public String ip() {
        return this.ip;
    }

    public int port() {
        return this.port;
    }
}
