package com.jsj.nettyrpc.common.client;

import com.jsj.nettyrpc.codec.CodeC;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private CodeC codeC;
    private ConnectionWatchDog connectionWatchDog;
    private ChannelHandler clientHandler;

    public ClientChannelInitializer( CodeC codeC, ConnectionWatchDog connectionWatchDog, ChannelHandler clientHandler) {
        this.codeC = codeC;
        this.connectionWatchDog = connectionWatchDog;
        this.clientHandler = clientHandler;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //
        pipeline.addLast(new IdleStateHandler(10, 5, 0, TimeUnit.SECONDS))
                //出方向编码
                .addLast(codeC.newEncoder())
                //入方向解码
                .addLast(codeC.newDecoder())
                //前置连接监视处理器
                .addLast(connectionWatchDog)
                //业务处理
                .addLast(clientHandler);
    }
}
