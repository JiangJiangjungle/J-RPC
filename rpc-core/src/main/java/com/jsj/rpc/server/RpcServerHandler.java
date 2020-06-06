package com.jsj.rpc.server;

import com.jsj.rpc.ChannelInfo;
import com.jsj.rpc.protocol.Packet;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.Request;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author jiangshenjie
 */
@Slf4j
@Getter
public class RpcServerHandler extends SimpleChannelInboundHandler<Packet> {
    private final RpcServer rpcServer;

    public RpcServerHandler(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        try {
            Channel channel = ctx.channel();
            ChannelInfo channelInfo = ChannelInfo.getOrCreateServerChannelInfo(channel);
            Protocol protocol = channelInfo.getProtocol();
            Request request = protocol.decodeAsRequest(packet);
            log.debug("New rpc request: {}.", request);
            rpcServer.getWorkerThreadPool().submit(new ServerWorkTask(request, protocol, channel));
        } finally {
            packet.release();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelInfo channelInfo = ChannelInfo.getOrCreateServerChannelInfo(ctx.channel());
        channelInfo.setProtocol(rpcServer.getProtocol());
        log.debug("Channel [remote addr: {}] active", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel [remote addr: {}] inactive.", ctx.channel().remoteAddress());
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx.channel().isActive() && !(cause instanceof IOException)) {
            log.info("Service exception, ex={}", cause.getMessage());
        }
        log.debug("Meet exception, may be connection is closed, msg={}", cause.getMessage());
        ctx.close();
    }
}
