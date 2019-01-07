package com.jsj.rpc.client;

import com.jsj.rpc.common.RpcFuture;
import com.jsj.rpc.common.RpcFutureHolder;
import com.jsj.rpc.common.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端的Handler
 *
 * @author jsj
 * @date 2018-10-4
 */
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    public ClientHandler() {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        Integer requestId = rpcResponse.getRequestId();
        RpcFuture future = RpcFutureHolder.removeFuture(channelHandlerContext.channel(), requestId);
        if (future != null && !future.isCancelled()) {
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
