package com.jsj.rpc.client;

import com.jsj.rpc.RpcProxy;
import com.jsj.rpc.codec.CodeC;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static ChannelHandler clientHandler = new ClientHandler();
    private CodeC codeC;
    private ConnectionWatchDog connectionWatchDog;

    public ClientChannelInitializer(CodeC codeC, ReConnectionListener reConnectionListener) {
        this.codeC = codeC;
        this.connectionWatchDog = new ConnectionWatchDog(reConnectionListener);
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //
        pipeline.addLast(new IdleStateHandler(RpcProxy.CONNECTION_READ_IDLE, RpcProxy.CONNECTION_WRITE_IDLE,
                0, TimeUnit.MILLISECONDS))
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
