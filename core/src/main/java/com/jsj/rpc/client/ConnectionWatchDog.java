package com.jsj.rpc.client;

import com.jsj.rpc.protocol.*;
import com.jsj.rpc.ChannelDataHolder;
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
public class ConnectionWatchDog extends SimpleChannelInboundHandler<Message> implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionWatchDog.class);
    private ReConnectionListener listener;

    public ConnectionWatchDog(ReConnectionListener reConnectionListener) {
        this.listener = reConnectionListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        //若是心跳响应则直接返回，否则交给下一handler处理
        byte messageType = message.getHeader().messageType();
        if (!message.emptyBody() && MessageTypeEnum.RPC_RESPONSE.getValue() == messageType) {
            LOGGER.info("Received RPC Response from server：{}", message);
            channelHandlerContext.fireChannelRead(message.getBody());
        } else if (message.emptyBody() && MessageTypeEnum.HEART_BEAT_RESPONSE.getValue() == messageType) {
            LOGGER.info("Received HeartBeat Response from server：{}", message);
        } else {
            LOGGER.error("Received Error Message Type：{}", message);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //当channel关闭时，清除eventLoop中channel对应的所有future
        ChannelDataHolder.removeChannel(ctx.channel());
        LOGGER.info("Channel closed, trying to reconnect.");
        //线程开启定时任务，准备尝试重连
        ctx.channel().eventLoop().schedule(this, 3L, TimeUnit.SECONDS);
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
                    LOGGER.debug("Send HeartBeat，channel：{}", ctx.channel());
                    ctx.writeAndFlush(MessageUtil.createHeartBeatRequestMessage());
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
        //不能在EventLoop中进行同步调用，这样会导致调用线程即EventLoop阻塞
        future.addListener(listener);
    }

    @Override
    public void run() {
        reConn(Connection.DEFAULT_CONNECT_TIMEOUT);
    }
}
