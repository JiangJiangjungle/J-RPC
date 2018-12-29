package com.jsj.rpc.client;


import com.jsj.rpc.NamedThreadFactory;
import com.jsj.rpc.codec.CodeC;
import com.jsj.rpc.common.RpcFuture;
import com.jsj.rpc.common.RpcRequest;
import com.jsj.rpc.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * RPC 客户端（用于发送 RPC 请求）
 *
 * @author jsj
 * @date 2018-10-10
 */
public class RpcClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    public static int MAP_CAPACITY = 1 << 10;
    public static float LOAD_FACTOR = 0.95f;

    public static int ID_MAX_VALUE = 1 << 16;

    private final String targetIP;
    private final int targetPort;

    private AtomicInteger requestId = new AtomicInteger(0);

    public static Map<Integer, RpcFuture> futureMap = new ConcurrentHashMap<>(MAP_CAPACITY, LOAD_FACTOR);

    /**
     * writeAndFlush（）实际是提交一个task到EventLoopGroup，所以channel是可复用的
     */
    private Connection connection = new Connection();

    private static ChannelHandler clientHandler = new ClientHandler(futureMap);

    /**
     * 配置客户端 NIO 线程组
     */
    private static EventLoopGroup group = new NioEventLoopGroup(1, new NamedThreadFactory("Rpc-netty-client", false));
    /**
     * 创建并初始化 Netty 客户端 Bootstrap 对象
     */
    private static Bootstrap bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
            //禁用nagle算法
            .option(ChannelOption.TCP_NODELAY, true);

    /**
     * 编解码方案
     *
     * @param targetIP
     * @param targetPort
     */
    private CodeC codeC;

    public RpcClient(String targetIP, int targetPort, CodeC codeC) {
        this.targetIP = targetIP;
        this.targetPort = targetPort;
        this.codeC = codeC;
        this.init();
    }

    private void init() {
        connection = new Connection(targetIP, targetPort, bootstrap);
        ReConnectionListener reConnectionListener = new ReConnectionListener(connection);
        RpcClient.bootstrap.handler(new ClientChannelInitializer(this.codeC, reConnectionListener, clientHandler));
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
        //注册到futureMap
        RpcRequest request = this.buildRpcRequest(method, parameters);
        Integer requestId = request.getRequestId();
        RpcFuture future = new RpcFuture(requestId);
        RpcResponse rpcResponse;
        try {
            futureMap.put(requestId, future);
            //发出请求，并直接返回
            this.getChannel().writeAndFlush(request);
            LOGGER.info("rpc request:{}, 同步调用", request.toString());
            rpcResponse = future.get(RpcFuture.MAX_WAIT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException t) {
            //调用超时
            LOGGER.info("rpc request:{}, 调用超时", request.toString());
            throw t;
        } finally {
            futureMap.remove(requestId);
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
    public RpcFuture invokeWithFuture(Method method, Object[] parameters) throws Exception {
        //注册到futureMap
        RpcRequest request = this.buildRpcRequest(method, parameters);
        Integer requestId = request.getRequestId();
        RpcFuture future = new RpcFuture(requestId);
        try {
            futureMap.put(requestId, future);
            //发出请求，并直接返回
            this.getChannel().writeAndFlush(request);
            LOGGER.info("rpc request:{}, 异步调用", request.toString());
        } catch (Exception e) {
            futureMap.remove(requestId);
            throw e;
        }
        return future;
    }

    public Channel getChannel() throws Exception {
        Channel channel = connection.get();
        if (channel == null) {
            channel = doCreateConnection(this.targetIP, this.targetPort, Connection.DEFAULT_CONNECT_TIMEOUT);
            connection.bind(channel);
        }
        //重连失败则抛出异常
        if (!channel.isActive()) {
            throw new ConnectTimeoutException();
        }
        return channel;
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
    private Channel doCreateConnection(String targetIP, int targetPort, int connectTimeout) throws Exception {
        // prevent unreasonable value, at least 1000
        connectTimeout = Math.max(connectTimeout, 1000);
        String address = targetIP + ":" + targetPort;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("connectTimeout of address [{}] is [{}].", address, connectTimeout);
        }
        RpcClient.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        //连接到远程节点，阻塞等待直到连接完成
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
        if (requestId.get() >= ID_MAX_VALUE) {
            synchronized (this) {
                if (requestId.get() >= ID_MAX_VALUE) {
                    requestId.getAndSet(0);
                }
            }
        }
        //设置请求id
        int id = requestId.getAndIncrement();
        request.setRequestId(id);
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
