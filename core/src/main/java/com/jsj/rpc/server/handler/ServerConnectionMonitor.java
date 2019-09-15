package com.jsj.rpc.server.handler;

import com.jsj.rpc.common.channel.ChannelDataHolder;
import com.jsj.rpc.common.message.Header;
import com.jsj.rpc.common.message.Message;
import com.jsj.rpc.common.message.MessageTypeEnum;
import com.jsj.rpc.util.MessageUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiangshenjie
 */
public class ServerConnectionMonitor extends SimpleChannelInboundHandler<Message> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnectionMonitor.class);

    public ServerConnectionMonitor() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        Header header = message.getHeader();
        //刷新最近请求时间
        ChannelDataHolder.updateChannel(channelHandlerContext.channel());
        //若是心跳请求则直接返回，否则交给下一handler处理
        if (MessageTypeEnum.HEART_BEAT_REQUEST.getValue() == header.messageType()) {
            LOGGER.debug("服务端收到心跳请求，channel:" + channelHandlerContext.channel());
            channelHandlerContext.writeAndFlush(MessageUtil.createHeartBeatResponse());
        } else {
            channelHandlerContext.fireChannelRead(message);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //当channel关闭时，清除ThreadLocal中channel的所有缓存
        Channel channel = ctx.channel();
        ChannelDataHolder.removeChannel(channel);
        LOGGER.info("Channel: {} closed.", channel);
        ctx.fireChannelInactive();
    }
}
