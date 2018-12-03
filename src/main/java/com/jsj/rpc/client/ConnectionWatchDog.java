package com.jsj.rpc.client;

import com.jsj.rpc.common.Header;
import com.jsj.rpc.common.NettyMessage;
import com.jsj.rpc.util.MessageUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


/**
 * @author jsj
 * @date 2018-10-24
 */
@ChannelHandler.Sharable
public class ConnectionWatchDog extends SimpleChannelInboundHandler<NettyMessage> implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionWatchDog.class);
    private ReConnectionListener listener;

    public ConnectionWatchDog(ReConnectionListener reConnectionListener) {
        this.listener = reConnectionListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NettyMessage message) throws Exception {
        //若是心跳响应则直接返回，否则交给下一handler处理
        Header header = message.getHeader();
        if (Header.RPC_RESPONSE == header.getType()) {
            channelHandlerContext.fireChannelRead(message.getContent());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.debug("链接关闭，将进行重连.");
        ctx.channel().eventLoop().schedule(this, 5L, TimeUnit.SECONDS);
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
                    ctx.writeAndFlush(MessageUtil.buildHeartBeatRequest());
                    break;
                default:
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void closeChannel(ChannelHandlerContext ctx) {
        Connection connection = listener.getConnection();
        connection.unbind();
        ctx.close();
    }

    private void reConn(int connectTimeout) {
        Connection connection = listener.getConnection();
        Bootstrap bootstrap = connection.getBootstrap();
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        ChannelFuture future = bootstrap.connect(connection.getTargetIP(), connection.getTargetPort());
        future.addListener(listener);
    }

    @Override
    public void run() {
        reConn(Connection.DEFAULT_CONNECT_TIMEOUT);
    }
}
