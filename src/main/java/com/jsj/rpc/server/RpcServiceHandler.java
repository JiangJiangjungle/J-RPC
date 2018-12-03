package com.jsj.rpc.server;

import com.jsj.rpc.common.RpcRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * RPC server（用于处理RPC Service请求）
 *
 * @author jsj
 * @date 2018-10-8
 */
@ChannelHandler.Sharable
public class RpcServiceHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceHandler.class);

    private ExecutorService threadPool;

    public RpcServiceHandler(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        LOGGER.info("服务端收到 rpc request:{}", request.toString());
        //交由业务线程池执行
        threadPool.execute(new RpcTask(ctx, request));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }
}
