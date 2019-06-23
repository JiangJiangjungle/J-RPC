package com.jsj.rpc.server;

import com.jsj.rpc.codec.CodeC;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private CodeC codeC;
    private ChannelHandler serverHandler;

    public ServerChannelInitializer(CodeC codeC, ChannelHandler clientHandler) {
        this.codeC = codeC;
        this.serverHandler = clientHandler;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //
        pipeline//出方向编码
                .addLast(codeC.newEncoder())
                //入方向解码
                .addLast(codeC.newDecoder())
                .addLast(new ServerConnectionHandler())
                //业务处理
                .addLast(serverHandler);
    }
}
