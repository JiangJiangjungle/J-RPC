package com.jsj.rpc.server;

import com.jsj.rpc.codec.serializer.SerializerTypeEnum;
import com.jsj.rpc.protocol.Message;
import com.jsj.rpc.protocol.RpcRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC server（用于处理RPC Service请求）
 *
 * @author jsj
 * @date 2018-10-8
 */
@ChannelHandler.Sharable
public class RpcServiceChannelHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceChannelHandler.class);

    private TaskExecutor executor;

    public RpcServiceChannelHandler(TaskExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, Message message) throws Exception {
        LOGGER.info("服务端收到 rpc request:{}", message.getBody().toString());
        //交由业务线程池执行
        SerializerTypeEnum serializationType = SerializerTypeEnum.get(message.getHeader().serializationType());
        executor.execute(ctx, (RpcRequest) message.getBody(), serializationType);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }
}
