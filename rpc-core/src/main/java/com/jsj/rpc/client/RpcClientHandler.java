package com.jsj.rpc.client;

import com.jsj.rpc.ChannelInfo;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.RpcPacket;
import com.jsj.rpc.protocol.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jiangshenjie
 */
@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcPacket> {
    private final RpcClient rpcClient;

    public RpcClientHandler(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcPacket packet) throws Exception {
        try {
            Channel channel = ctx.channel();
            ChannelInfo channelInfo = ChannelInfo.getOrCreateClientChannelInfo(channel);
            Protocol protocol = channelInfo.getProtocol();
            RpcResponse response = protocol.decodeResponse(packet, channelInfo);
            log.debug("New rpc response: {}.", response);
            DefaultRpcFuture<?> rpcFuture = channelInfo.getAndRemoveRpcFuture(response.getRequestId());
            //在业务线程执行回调函数
            rpcClient.getWorkerThreadPool().submit(() -> {
                rpcFuture.setResponse(response);
            });
        } catch (Exception e) {
            log.error("error before handle rpc response.",e);
        } finally {
            packet.release();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelInfo channelInfo = ChannelInfo.getOrCreateClientChannelInfo(ctx.channel());
        channelInfo.setProtocol(rpcClient.getProtocol());
        log.info("Channel [remote address: {}] active.", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //注销channel
        rpcClient.getChannelManager().removeChannel(ctx.channel());
    }
}
