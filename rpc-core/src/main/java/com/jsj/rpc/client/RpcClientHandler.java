package com.jsj.rpc.client;

import com.jsj.rpc.ChannelInfo;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.protocol.Packet;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jiangshenjie
 */
@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<Packet> {
    private final RpcClient rpcClient;

    public RpcClientHandler(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        try {
            Channel channel = ctx.channel();
            ChannelInfo channelInfo = ChannelInfo.getOrCreateClientChannelInfo(channel);
            Protocol protocol = channelInfo.getProtocol();
            Response response = protocol.decodeAsResponse(packet, channelInfo);
            RpcFuture<?> rpcFuture = response.getRpcFuture();
            log.debug("Get new rpc response: {}.", response);
            //在业务线程处理结果
            final Response rpcResponse = response;
            rpcClient.getWorkerThreadPool().submit(() -> {
                rpcFuture.handleResponse(rpcResponse);
            });
        } finally {
            packet.release();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelInfo channelInfo = ChannelInfo.getOrCreateClientChannelInfo(ctx.channel());
        channelInfo.setProtocol(rpcClient.getProtocol());
        log.info("Channel {} is active.", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel {} is inactive.", ctx.channel());
        //注销channel
        rpcClient.getRpcChannel().removeChannel(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Exception caught in rpc client handler chain", cause);
        ctx.close();
    }
}
