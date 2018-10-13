package com.jsj.nettyrpc.common.client;

import com.jsj.nettyrpc.common.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class ClientChannelHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientChannelHandler.class);

    private ConcurrentHashMap<String, RpcResponse> responseMap;

    public ClientChannelHandler(ConcurrentHashMap<String, RpcResponse> responseMap) {
        this.responseMap = responseMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        this.responseMap.put(rpcResponse.getRequestId(), rpcResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("api caught exception", cause);
        ctx.channel().close();
    }
}
