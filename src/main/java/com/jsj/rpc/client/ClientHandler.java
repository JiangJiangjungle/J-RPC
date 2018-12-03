package com.jsj.rpc.client;

import com.jsj.rpc.common.RpcFuture;
import com.jsj.rpc.common.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 客户端的Handler
 *
 * @author jsj
 * @date 2018-10-4
 */
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    /**
     * 异步调用所注册的Future对象
     */
    private Map<Integer, RpcFuture> futureMap;

    public ClientHandler(Map<Integer, RpcFuture> futureMap) {
        this.futureMap = futureMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        Integer requestId = rpcResponse.getRequestId();
        RpcFuture future = futureMap.remove(requestId);
        //更新对应的RpcFuture
        future.done(rpcResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("api caught exception", cause);
        ctx.channel().close();
    }
}
