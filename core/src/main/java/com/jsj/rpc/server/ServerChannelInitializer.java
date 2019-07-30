package com.jsj.rpc.server;

import com.jsj.rpc.codec.CodeC;
import com.jsj.rpc.task.ChannelRemoveTask;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.TimeUnit;

/**
 * @author jiangshenjie
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private CodeC codeC;
    private ChannelHandler serverHandler;
    private int channelAliveTime;

    public ServerChannelInitializer(CodeC codeC, ChannelHandler serverHandler, int channelAliveTime) {
        this.codeC = codeC;
        this.serverHandler = serverHandler;
        this.channelAliveTime = channelAliveTime;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        EventLoop eventLoop = socketChannel.eventLoop();
        //创建定时任务：检查channel是否过期并删除
        eventLoop.schedule(new ChannelRemoveTask(socketChannel, eventLoop, channelAliveTime), channelAliveTime, TimeUnit.MILLISECONDS);
        ChannelPipeline pipeline = socketChannel.pipeline();
        //
        pipeline//出方向编码
                .addLast(codeC.newEncoder())
                //入方向解码
                .addLast(codeC.newDecoder())
                .addLast(new ServerConnectionChannelHandler())
                //业务处理
                .addLast(serverHandler);
    }
}
