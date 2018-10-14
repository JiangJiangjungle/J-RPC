package com.jsj.nettyrpc.common.client;

import com.jsj.nettyrpc.common.RpcResponse;
import com.jsj.nettyrpc.common.RpcFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class ClientChannelHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientChannelHandler.class);

    private ConcurrentHashMap<String, RpcResponse> responseMap;

    /**
     * 异步调用所注册的Future对象
     */
    private ConcurrentHashMap<String, RpcFuture> futureMap;

    public ClientChannelHandler(ConcurrentHashMap<String, RpcResponse> responseMap, ConcurrentHashMap<String, RpcFuture> futureMap) {
        this.responseMap = responseMap;
        this.futureMap = futureMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        String requestId = rpcResponse.getRequestId();
        RpcFuture future = futureMap.get(requestId);
        //判断是同步调用还是异步调用
        if (future == null) {
            //若是同步调用则存入responseMap
            this.responseMap.put(rpcResponse.getRequestId(), rpcResponse);
        } else {
            future.setRpcResponse(rpcResponse);
            future.setDone(true);
            futureMap.remove(requestId);
        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("api caught exception", cause);
        ctx.channel().close();
    }
}
