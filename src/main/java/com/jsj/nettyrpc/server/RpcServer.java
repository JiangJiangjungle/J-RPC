package com.jsj.nettyrpc.server;

import com.jsj.nettyrpc.RpcService;
import com.jsj.nettyrpc.codec.RpcDecoder;
import com.jsj.nettyrpc.codec.RpcEncoder;
import com.jsj.nettyrpc.common.RpcRequest;
import com.jsj.nettyrpc.common.RpcResponse;
import com.jsj.nettyrpc.registry.ServiceRegistry;
import com.jsj.nettyrpc.util.StringUtil;
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

/**
 * RPC server（用于注册RPC Service）
 *
 * @author jsj
 * @date 2018-10-8
 */
public class RpcServer implements RemotingServer, ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String ip;

    private int port;

    private ServiceRegistry serviceRegistry;

    /**
     * 开放RPC的服务列表缓存
     */
    private Map<String, Object> handlerMap = new HashMap<>();

    public RpcServer(String ip, int port, ServiceRegistry serviceRegistry) {
        this.ip = ip;
        this.port = port;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * 扫描带有 RpcService 注解的服务并添加到 handlerMap ，之后用于服务注册
     *
     * @throws Exception
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 扫描带有 RpcService 注解的类并初始化 handlerMap 对象
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isEmpty(serviceBeanMap)) {
            return;
        }
        //遍历后取出所有serviceName和对应的Class对象
        serviceBeanMap.forEach((key, serviceBean) -> {
            RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
            String serviceName = rpcService.value().getName();
            String serviceVersion = rpcService.version();
            if (StringUtil.isNotEmpty(serviceVersion)) {
                serviceName += "-" + serviceVersion;
            }
            handlerMap.put(serviceName, serviceBean);
        });
    }

    /**
     * 创建Netty RPC服务器服务端，注册（到服务中心），监听和处理客户端的RPC调用请求
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //配置服务端 NIO 线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //创建并初始化 Netty 服务端辅助启动对象 ServerBootstrap
            ServerBootstrap bootstrap = this.initServerBootstrap(bossGroup, workerGroup);
            // 绑定对应ip和端口，同步等待成功
            ChannelFuture future = bootstrap.bind(ip, port).sync();
            // 将服务地址添加到服务注册中心
            this.registerAllService();
            LOGGER.debug("server started on port {}", port);
            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } finally {
            //优雅退出，释放 NIO 线程组
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public String ip() {
        return this.ip;
    }

    @Override
    public int port() {
        return this.port;
    }

    @Override
    public void registerAllService() {
        if (this.serviceRegistry != null) {
            String serviceAddress = this.ip + ":" + this.port;
            for (String interfaceName : handlerMap.keySet()) {
                serviceRegistry.register(interfaceName, serviceAddress);
                LOGGER.debug("register service: {} => {}", interfaceName, serviceAddress);
            }
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
                                .addLast(new RpcServiceHandler(handlerMap));
                    }
                });
    }
}
