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
        ctx.channel().eventLoop().schedule(this, 3L, TimeUnit.SECONDS);
        ctx.fireChannelInactive();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        Channel channel = ctx.channel();
        //只检查心跳连接
        if (evt instanceof IdleStateEvent && listener.getConnectionHolder().getChannel() == channel) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    LOGGER.debug("发送心跳包，channel：{}", channel);
                    closeChannel(ctx);
                    break;
                case WRITER_IDLE:
                    LOGGER.debug("发送心跳包，channel：{}", channel);
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
        HeartBeatConnectionHolder connectionHolder = listener.getConnectionHolder();
        connectionHolder.unbind();
        ctx.close();
    }

    private void reConn(int connectTimeout) {
        HeartBeatConnectionHolder holder = listener.getConnectionHolder();
        Bootstrap bootstrap = holder.getBootstrap();
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        ChannelFuture future = bootstrap.connect(holder.getTargetIP(), holder.getPort());
        future.addListener(listener);
    }

    @Override
    public void run() {
        reConn(HeartBeatConnectionHolder.DEFAULT_CONNECT_TIMEOUT);
    }
}
