package com.jsj.rpc.server;

import com.jsj.rpc.common.Header;
import com.jsj.rpc.common.NettyMessage;
import com.jsj.rpc.util.MessageUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerConnectionHandler extends SimpleChannelInboundHandler<NettyMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnectionHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NettyMessage message) throws Exception {
        Header header = message.getHeader();
        //若是心跳请求则直接返回，否则交给下一handler处理
        if (Header.HEART_BEAT_REQUEST == header.getType()) {
            LOGGER.debug("服务端收到心跳请求，channel:" + channelHandlerContext.channel());
            channelHandlerContext.writeAndFlush(MessageUtil.buildHeartBeatResponse());
        } else {
            channelHandlerContext.fireChannelRead(message.getContent());
        }
    }
}
