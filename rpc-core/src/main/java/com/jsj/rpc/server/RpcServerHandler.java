package com.jsj.rpc.server;

import com.jsj.rpc.ChannelInfo;
import com.jsj.rpc.protocol.Packet;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
            ChannelInfo channelInfo = ChannelInfo.getOrCreateServerChannelInfo(ctx.channel());
            Protocol protocol = channelInfo.getProtocol();
            Request request = protocol.decodeAsRequest(packet);
            log.debug("New rpc request: {}.", request);
            rpcServer.getWorkerThreadPool().submit(new ServerWorkTask(request, rpcServer, ctx));
        } finally {
            packet.release();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelInfo channelInfo = ChannelInfo.getOrCreateServerChannelInfo(ctx.channel());
        channelInfo.setProtocol(rpcServer.getProtocol());
        log.info("Channel [remote address: {}] active", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel [remote address: {}] closed", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("", cause);
    }
}
