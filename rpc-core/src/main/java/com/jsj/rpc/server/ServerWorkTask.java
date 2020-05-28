package com.jsj.rpc.server;

import com.jsj.rpc.protocol.ResponseMeta;
import com.jsj.rpc.protocol.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jiangshenjie
 */
@Slf4j
@Getter
@Setter
public class ServerWorkTask implements Runnable {
    private RpcRequest request;
    private RpcServer rpcServer;
    private ChannelHandlerContext ctx;

    public ServerWorkTask(RpcRequest request, RpcServer rpcServer, ChannelHandlerContext ctx) {
        this.request = request;
        this.rpcServer = rpcServer;
        this.ctx = ctx;
    }

    @Override
    public void run() {
        ResponseMeta meta = new ResponseMeta();
        meta.setRequestId(request.getRequestId());
        try {
            meta.setResult(request.getMethod().invoke(request.getTarget(), request.getParams()));
        } catch (Exception e) {
            log.warn("Execute ServerWorkTask error, request id: {}, err msg: {}."
                    , request.getRequestId(), e.getMessage());
            meta.setErrorMessage(String.format("%s: %s", e.getClass().getName(), e.getMessage()));
        }
        ctx.channel().writeAndFlush(meta)
                .addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("Send rpc response succeed, request id: {}.", meta.getRequestId());
                    } else {
                        log.warn("Send rpc response failed! request id: {}.", meta.getRequestId());
                    }
                });
    }
}
