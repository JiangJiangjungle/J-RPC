package com.jsj.nettyrpc.common.client;

import com.jsj.nettyrpc.common.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author jsj
 * @date 2018-10-24
 */
@ChannelHandler.Sharable
public class ConnectionWatchDog extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionWatchDog.class);
    private ReConnectionListener reConnectionListener;

    public ConnectionWatchDog(ReConnectionListener reConnectionListener) {
        this.reConnectionListener = reConnectionListener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.debug("链接关闭，将进行重连.");
        reConn(Connection.DEFAULT_CONNECT_TIMEOUT);
        ctx.fireChannelInactive();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    closeChannel(ctx);
                    break;
                case WRITER_IDLE:
                    LOGGER.debug("发送心跳包，channel：{}", ctx.channel());
                    sendHeartBeat(ctx);
                    break;
                default:
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void sendHeartBeat(ChannelHandlerContext ctx) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setHeartBeat(true);
        ctx.writeAndFlush(rpcRequest);
    }

    private void closeChannel(ChannelHandlerContext ctx) {
        Connection connection = reConnectionListener.getConnection();
        connection.delete();
        ctx.close();
    }

    private void reConn(int connectTimeout) {
        String targetIP = reConnectionListener.getTargetIP();
        int port = reConnectionListener.getPort();
        Bootstrap bootstrap = reConnectionListener.getBootstrap();
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        ChannelFuture future = bootstrap.connect(targetIP, port);
        future.addListener(reConnectionListener);
    }
}
