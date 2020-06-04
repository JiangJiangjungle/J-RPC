package com.jsj.rpc.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 关闭半开连接
 *
 * @author jiangshenjie
 */
@Slf4j
public class RpcServerChannelIdleHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent)) {
            ctx.fireUserEventTriggered(evt);
            return;
        }
        if (((IdleStateEvent) evt).state() == IdleState.ALL_IDLE) {
            Channel channel = ctx.channel();
            log.debug("Channel: [remote addr: {}] is idle for period time, close now."
                    , channel.remoteAddress());
            ctx.close();
        }
    }
}
