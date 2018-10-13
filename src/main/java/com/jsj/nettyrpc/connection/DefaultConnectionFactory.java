package com.jsj.nettyrpc.connection;

import com.jsj.nettyrpc.codec.CodeC;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 以工厂模式创建netty连接
 *
 * @author jsj
 * @date 2018-10-13
 */
public class DefaultConnectionFactory implements ConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionFactory.class);

    private ChannelHandler channelHandler;
    private CodeC codeC;

    private Bootstrap bootstrap;

    public DefaultConnectionFactory(ChannelHandler channelHandler, CodeC codeC) {
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
                        //解码
                        pipeline.addLast(codeC.newDecoder())
                                //编码
                                .addLast(codeC.newEncoder())
                                //处理
                                .addLast(channelHandler);
                    }
                });
    }

    @Override
    public Connection createConnection(String targetIP, int targetPort, int connectTimeout) throws Exception {
        Channel channel = doCreateConnection(targetIP, targetPort, connectTimeout);
        return new Connection(channel, targetIP, targetPort);
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
