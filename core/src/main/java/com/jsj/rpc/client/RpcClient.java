package com.jsj.rpc.client;


import com.jsj.rpc.ChannelDataHolder;
import com.jsj.rpc.DefaultRpcFuture;
import com.jsj.rpc.NamedThreadFactory;
import com.jsj.rpc.config.DefaultClientConfiguration;
import com.jsj.rpc.protocol.Message;
import com.jsj.rpc.protocol.MessageTypeEnum;
import com.jsj.rpc.protocol.RpcRequest;
import com.jsj.rpc.protocol.RpcResponse;
import com.jsj.rpc.util.MessageUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * RPC 客户端（用于发送 RPC 请求,对于同一目标IP+目标端口，RpcClient唯一）
 *
 * @author jsj
 * @date 2018-10-10
 */
public class RpcClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
    private static final int THRESHOLD = Integer.MAX_VALUE >> 1;

    /**
     * 配置客户端 NIO 线程组
     */
    private static EventLoopGroup group = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2,
            new NamedThreadFactory("Rpc-netty-client", false));

    private final String ip;
    private final int port;

    /**
     * 创建并初始化 Netty 客户端 Bootstrap 对象
     */
    private Bootstrap bootstrap;

    /**
     * 请求ID发放器
     */
    private AtomicInteger idCount;

    /**
     * 客户端连接配置项
     */
    private DefaultClientConfiguration configuration;

    /**
     * writeAndFlush（）实际是提交一个task到EventLoopGroup，所以channel是可复用的
     */
    private Connection connection;

    public RpcClient(String ip, int port, DefaultClientConfiguration configuration) {
        this.ip = ip;
        this.port = port;
        this.configuration = configuration;
        this.init();
    }

    private void init() {
        this.idCount = new AtomicInteger(0);
        this.bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, configuration.getTcpNoDelay());
        this.connection = new Connection(ip, port, bootstrap);
        ReConnectionListener reConnectionListener = new ReConnectionListener(connection);
        bootstrap.handler(new ClientChannelInitializer(this.configuration, reConnectionListener));
    }

    /**
     * 同步调用
     *
     * @param method
     * @param parameters
     * @return
     * @throws Exception
     */
    public RpcResponse invokeSync(Method method, Object[] parameters) throws Exception {
        RpcResponse rpcResponse;
        RpcRequest request = this.buildRpcRequest(method, parameters);
        LOGGER.info("Rpc Request:{}, Sync", request.toString());
        try {
            DefaultRpcFuture future = invoke(request);
            rpcResponse = future.get(configuration.getRpcRequestTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            //调用超时
            if (e instanceof TimeoutException) {
                LOGGER.info("Response Wait Timeout. Request:{}", request.toString());
            }
            throw e;
        }
        return rpcResponse;
    }

    /**
     * 异步调用
     *
     * @param method
     * @param parameters
     * @return
     * @throws Exception
     */
    public DefaultRpcFuture invokeWithFuture(Method method, Object[] parameters) throws Exception {
        RpcRequest request = this.buildRpcRequest(method, parameters);
        LOGGER.info("Rpc Request:{}, ASync", request.toString());
        return invoke(request);
    }

    /**
     * 获取Channel
     *
     * @return
     * @throws Exception
     */
    private Channel getChannel() throws Exception {
        Channel channel = connection.getChannel();
        if (channel == null) {
            synchronized (this) {
                channel = connection.getChannel();
                if (channel == null) {
                    channel = doCreateConnection(this.ip, this.port, configuration.getConnectTimeout());
                    connection.bind(channel);
                }
            }
        }
        //重连失败则抛出异常
        if (!channel.isActive()) {
            throw new ConnectTimeoutException();
        }
        return channel;
    }

    /**
     * 建立Channel连接
     *
     * @param ip
     * @param port
     * @param connectTimeout
     * @return
     * @throws Exception
     */
    private Channel doCreateConnection(String ip, int port, int connectTimeout) throws Exception {
        // prevent unreasonable value, at least 1000
        connectTimeout = Math.max(connectTimeout, 1000);
        String address = ip + ":" + port;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ConnectTimeout of address [{}] is [{}].", address, connectTimeout);
        }
        this.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        //建立tcp连接到远程节点，调用线程阻塞等待直到连接完成
        ChannelFuture future = bootstrap.connect(ip, port);
        future.awaitUninterruptibly();
        future.sync();
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

    /**
     * 实际调用方法,调用线程执行
     *
     * @param request
     * @return
     * @throws Exception
     */
    private DefaultRpcFuture invoke(RpcRequest request) throws Exception {
        //注册到futureMap
        DefaultRpcFuture future = new DefaultRpcFuture(request.getRequestId());
        Channel channel = null;
        ChannelDataHolder channelDataHolder = null;
        try {
            channel = getChannel();
            //eventLoop的缓存中添加RpcFuture
            channelDataHolder = new ChannelDataHolder(channel.eventLoop());
            channelDataHolder.set(channel, future);
            //封装Message，写入rpc请求
            Message rpcRequestMessage = MessageUtil.createMessage(MessageTypeEnum.RPC_REQUEST, this.configuration.getSerializerTypeEnum(), request);
            channel.writeAndFlush(rpcRequestMessage);
        } catch (Exception e) {
            //eventLoop的缓存中删除RpcFuture
            if (channel != null && channelDataHolder != null) {
                channelDataHolder.remove(channel, future);
            }
            throw e;
        }
        return future;
    }

    /**
     * 创建并初始化 RpcRequest
     *
     * @param method
     * @param parameters
     * @return
     */
    private RpcRequest buildRpcRequest(Method method, Object[] parameters) {
        RpcRequest request = new RpcRequest();
        //若当前计数器值超过阈值，需要重置
        if (idCount.get() >= THRESHOLD) {
            synchronized (this) {
                if (idCount.get() >= THRESHOLD) {
                    idCount.getAndSet(0);
                }
            }
        }
        //设置请求id
        request.setRequestId(idCount.getAndIncrement());
        //设置服务接口名称
        request.setInterfaceName(method.getDeclaringClass().getName());
        //设置调用方法名
        request.setMethodName(method.getName());
        //设置参数类型
        request.setParameterTypes(method.getParameterTypes());
        //设置参数值
        request.setParameters(parameters);
        return request;
    }
}
