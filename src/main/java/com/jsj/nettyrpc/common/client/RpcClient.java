package com.jsj.nettyrpc.common.client;


import com.jsj.nettyrpc.codec.CodeC;
import com.jsj.nettyrpc.codec.DefaultCodeC;
import com.jsj.nettyrpc.common.RpcRequest;
import com.jsj.nettyrpc.common.RpcResponse;
import com.jsj.nettyrpc.common.RpcFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

import java.util.concurrent.ConcurrentHashMap;


/**
 * RPC 客户端（用于发送 RPC 请求）
 *
 * @author jsj
 * @date 2018-10-10
 */
public class RpcClient {
    private final String ip;
    private final int port;

    private Channel channel;

    private ConcurrentHashMap<String, RpcResponse> responseMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, RpcFuture> futureMap = new ConcurrentHashMap<>();
    private ChannelHandler channelHandler;
    private CodeC codeC;
    private ConnectionFactory connectionFactory;

    public RpcClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void init() throws Exception {
        channelHandler = new ClientChannelHandler(responseMap, futureMap);
        codeC = new DefaultCodeC(RpcRequest.class, RpcResponse.class);
        connectionFactory = new DefaultConnectionFactory(this.channelHandler, this.codeC);
        connectionFactory.init();
        channel = connectionFactory.createConnection(ip, port);
    }

    /**
     * 同步调用
     *
     * @param request
     * @return
     * @throws Exception
     */
    public RpcResponse invokeSync(RpcRequest request) throws Exception {
        this.checkChannel();
        //发出请求，sync()阻塞至 request写出完成
        channel.writeAndFlush(request).sync();
        //等待handler处理完毕，再获取response
        RpcResponse rpcResponse;
        do {
            rpcResponse = responseMap.remove(request.getRequestId());
        } while (rpcResponse == null);
        return rpcResponse;
    }

    /**
     * 异步调用
     *
     * @param request
     * @return
     * @throws Exception
     */
    public RpcFuture invokeWithFuture(RpcRequest request) throws Exception {
        //注册到futureMap
        String requestId = request.getRequestId();
        RpcFuture future = new RpcFuture(request);
        futureMap.put(requestId, future);
        //发出请求，并直接返回
        channel.writeAndFlush(request);
        return future;
    }

    /**
     * 关闭channel
     *
     * @throws Exception
     */
    public void close() throws Exception {
        channel.close().sync();
    }

    /**
     * 检查channel 是否连接
     *
     * @throws Exception
     */
    private void checkChannel() throws Exception {
        if (!channel.isOpen()) {
            channel = connectionFactory.createConnection(ip, port);
        }
    }
}
