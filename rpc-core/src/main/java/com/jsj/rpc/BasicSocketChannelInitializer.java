package com.jsj.rpc;


import com.jsj.rpc.codec.BaseDecoder;
import com.jsj.rpc.codec.BaseEncoder;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.RpcProtocol;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * 初始化 SocketChannel
 *
 * @author jiangshenjie
 */
@ChannelHandler.Sharable
public class BasicSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final ChannelHandler[] channelHandlers;
    private final Protocol protocol;

    public BasicSocketChannelInitializer(ChannelHandler... channelHandlers) {
        this(new RpcProtocol(), channelHandlers);
    }

    public BasicSocketChannelInitializer(Protocol protocol, ChannelHandler... channelHandlers) {
        this.channelHandlers = channelHandlers;
        this.protocol = protocol;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                //出方向编码
                .addLast(new BaseEncoder(protocol))
                //入方向解码
                .addLast(new BaseDecoder(protocol))
                //业务处理
                .addLast(channelHandlers);
    }
}
