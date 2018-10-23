package com.jsj.nettyrpc.common.client;

import com.jsj.nettyrpc.common.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.IdleStateEvent;


import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionWatchDog extends ChannelInboundHandlerAdapter {

    private final Bootstrap bootstrap;
    private final String targetIP;
    private final int port;

    private AtomicBoolean occupied = new AtomicBoolean(false);
    private AtomicBoolean connected = new AtomicBoolean(false);
    private int attempts = 3;

    public ConnectionWatchDog(Bootstrap bootstrap, String targetIP, int port) {
        this.bootstrap = bootstrap;
        this.targetIP = targetIP;
        this.port = port;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("链接关闭，将进行重连: " + LocalTime.now());
        while (attempts > 0 && !reConnect(ctx, 2000)) {
            attempts--;
        }
        if (!connected.get()) {
            System.out.println("重连失败，且已经达到最大重试次数，不再重试: " + LocalTime.now());
            closeChannel(ctx);
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    System.out.println("READER_IDLE: " + LocalTime.now());
                    closeChannel(ctx);
                    break;
                case WRITER_IDLE:
                    System.out.println("WRITER_IDLE: " + LocalTime.now());
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
        System.out.println("client 发送 PING...");
    }

    private void closeChannel(ChannelHandlerContext ctx) {
        ctx.close();
        connected.getAndSet(false);
    }

    public boolean reConnect(ChannelHandlerContext ctx, int connectTimeout) throws InterruptedException {
        ChannelFuture future;
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        future = bootstrap.connect(targetIP, port);
        long start = System.currentTimeMillis();
        while (!future.isDone() && System.currentTimeMillis() - start < connectTimeout) {

        }
        return future.isSuccess();
    }

}
