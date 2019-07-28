package com.jsj.rpc.client;

import com.jsj.rpc.config.DefaultClientConfiguration;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author jiangshenjie
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static ChannelHandler clientHandler = new ClientHandler();
    private DefaultClientConfiguration configuration;
    private ConnectionWatchDog connectionWatchDog;

    public ClientChannelInitializer(DefaultClientConfiguration configuration, ReConnectionListener reConnectionListener) {
        this.configuration = configuration;
        this.connectionWatchDog = new ConnectionWatchDog(reConnectionListener);
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //
        pipeline.addLast(new IdleStateHandler(configuration.getReadIdle(), configuration.getWriteIdle(),
                0, TimeUnit.MILLISECONDS))
                //出方向编码
                .addLast(configuration.getCodeC().newEncoder())
                //入方向解码
                .addLast(configuration.getCodeC().newDecoder())
                //前置连接监视处理器
                .addLast(connectionWatchDog)
                //业务处理
                .addLast(clientHandler);
    }
}
