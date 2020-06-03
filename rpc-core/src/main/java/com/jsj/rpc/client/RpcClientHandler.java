package com.jsj.rpc.client;

import com.jsj.rpc.ChannelInfo;
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
        Response response = null;
        try {
            Channel channel = ctx.channel();
            ChannelInfo channelInfo = ChannelInfo.getOrCreateClientChannelInfo(channel);
            Protocol protocol = channelInfo.getProtocol();
            response = protocol.decodeAsResponse(packet, channelInfo);
            log.debug("New rpc response: {}.", response);
            //在业务线程执行回调函数
            final Response rpcResponse = response;
            rpcClient.getWorkerThreadPool().submit(() -> {
                try {
                    rpcResponse.getRpcFuture().handleResponse(rpcResponse);
                } catch (Exception e) {
                    log.error("error when handling rpc response:{}.", rpcResponse, e);
                }
            });
        } catch (Exception e) {
            log.error("error before handle rpc response:{}.", response, e);
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
        rpcClient.getChannelManager().removeAndCloseChannel(ctx.channel());
        log.info("Channel [remote address: {}] closed.", ctx.channel().remoteAddress());
    }
}
