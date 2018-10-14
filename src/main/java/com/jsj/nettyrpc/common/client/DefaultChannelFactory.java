package com.jsj.nettyrpc.common.client;

import com.jsj.nettyrpc.codec.CodeC;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 以工厂模式创建channel
 *
 * @author jsj
 * @date 2018-10-13
 */
public class DefaultChannelFactory implements ChannelFactory {

    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultChannelFactory.class);

    private ChannelHandler channelHandler;
    private CodeC codeC;

    private Bootstrap bootstrap;

    public DefaultChannelFactory(ChannelHandler channelHandler, CodeC codeC) {
        this.channelHandler = channelHandler;
        this.codeC = codeC;
    }

    @Override
    public void init() {
        //配置客户端 NIO 线程组
        EventLoopGroup group = new NioEventLoopGroup();
        // 创建并初始化 Netty 客户端 Bootstrap 对象
        this.bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        //出方向编码
                        pipeline.addLast(codeC.newEncoder())
                                //入方向解码
                                .addLast(codeC.newDecoder())
                                //处理
                                .addLast(channelHandler);
                    }
                });
    }

    @Override
    public Channel createConnection(String targetIP, int targetPort) throws Exception {
        return this.createConnection(targetIP, targetPort, DEFAULT_CONNECT_TIMEOUT);
    }

    @Override
    public Channel createConnection(String targetIP, int targetPort, int connectTimeout) throws Exception {
        return doCreateConnection(targetIP, targetPort, connectTimeout);
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
    protected Channel doCreateConnection(String targetIP, int targetPort, int connectTimeout) throws Exception {
        // prevent unreasonable value, at least 1000
        connectTimeout = Math.max(connectTimeout, 1000);
        String address = targetIP + ":" + targetPort;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("connectTimeout of address [{}] is [{}].", address, connectTimeout);
        }
        this.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        //连接到远程节点，阻塞等待直到连接完成
        ChannelFuture future = bootstrap.connect(targetIP, targetPort).sync();
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
