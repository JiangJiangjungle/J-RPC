package com.jsj.rpc.client.handler;

import com.jsj.rpc.client.Connection;
import com.jsj.rpc.client.ReConnectionListener;
import com.jsj.rpc.common.channel.ChannelDataHolder;
import com.jsj.rpc.common.message.Body;
import com.jsj.rpc.common.message.Message;
import com.jsj.rpc.common.message.MessageTypeEnum;
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
public class ClientConnectionMonitor extends SimpleChannelInboundHandler<Message> implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectionMonitor.class);
    private ReConnectionListener listener;

    public ClientConnectionMonitor(ReConnectionListener reConnectionListener) {
        this.listener = reConnectionListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        //若是心跳响应则直接返回，否则交给下一handler处理
        byte messageType = message.getHeader().messageType();
        Channel channel = channelHandlerContext.channel();
        //rpc响应
        if (!message.emptyBody() && MessageTypeEnum.RPC_RESPONSE.getValue() == messageType) {
            Body response = message.getBody();
            LOGGER.info("RPC Response: [{}] From [{}].", response, channel.remoteAddress());
            channelHandlerContext.fireChannelRead(response);
        }//心跳响应
        else if (message.emptyBody() && MessageTypeEnum.HEART_BEAT_RESPONSE.getValue() == messageType) {
            LOGGER.info("HeartBeat Response From [{}].", channel.remoteAddress());
        } else {
            LOGGER.error("Error Message: [{}] From [{}].", message, channel.remoteAddress());
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
        LOGGER.info("Trying to reconnect.");
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
                    //写入心跳请求
                    ctx.writeAndFlush(MessageUtil.createHeartBeatRequest());
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

    /**
     * 重连逻辑
     *
     * @param connectTimeout
     */
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
