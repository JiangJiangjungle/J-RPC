package com.jsj.nettyrpc.common.client;

import com.jsj.nettyrpc.common.RpcResponse;
import com.jsj.nettyrpc.common.RpcFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端的Handler
 *
 * @author jsj
 * @date 2018-10-4
 */
public class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    /**
     * 异步调用所注册的Future对象
     */
    private ConcurrentHashMap<Integer, RpcFuture> futureMap;

    public ClientHandler(ConcurrentHashMap<Integer, RpcFuture> futureMap) {
        this.futureMap = futureMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        if (!rpcResponse.isHeartBeat()) {
            Integer requestId = rpcResponse.getRequestId();
            RpcFuture future = futureMap.remove(requestId);
            //更新对应的RpcFuture
            future.done(rpcResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("api caught exception", cause);
        ctx.channel().close();
    }
}
