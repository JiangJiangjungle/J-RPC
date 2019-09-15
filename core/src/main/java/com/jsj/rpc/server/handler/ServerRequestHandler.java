package com.jsj.rpc.server.handler;

import com.jsj.rpc.RpcRequest;
import com.jsj.rpc.common.message.Message;
import com.jsj.rpc.common.message.MessageTypeEnum;
import com.jsj.rpc.common.serializer.SerializerTypeEnum;
import com.jsj.rpc.server.BusinessTaskExecutor;
import com.jsj.rpc.util.MessageUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC Service请求处理器：单例
 *
 * @author jsj
 * @date 2018-10-8
 */
@ChannelHandler.Sharable
public class ServerRequestHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRequestHandler.class);

    /**
     * 业务线程池
     */
    private BusinessTaskExecutor executor;

    public ServerRequestHandler(BusinessTaskExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, Message message) throws Exception {
        //交由业务线程池执行
        SerializerTypeEnum serializationType = SerializerTypeEnum.get(message.getHeader().serializationType());
        RpcRequest request = (RpcRequest) message.getBody();
        LOGGER.info("Request received: [{}]", request.toString());
        executor.execute(request, response -> {
            //如果在对应 Netty IO线程调用会直接write；否则将作为一个task插入到 Netty IO线程执行
            ctx.writeAndFlush(MessageUtil.createMessage(MessageTypeEnum.RPC_RESPONSE, serializationType, response));
            LOGGER.info("Request [{}] finished!", request.getRequestId());
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Server caught exception", cause);
        ctx.close();
    }
}
