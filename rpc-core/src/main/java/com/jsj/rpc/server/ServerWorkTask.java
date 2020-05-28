package com.jsj.rpc.server;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.jsj.rpc.protocol.RpcMeta;
import com.jsj.rpc.protocol.RpcPacket;
import com.jsj.rpc.protocol.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
        RpcMeta.ResponseMeta.Builder builder = RpcMeta.ResponseMeta.newBuilder();
        builder.setRequestId(request.getRequestId());
        try {
            //必须是com.google.protobuf.Message的子类
            Message result = (Message) request.getMethod().invoke(request.getTarget(), request.getParams());
            builder.setResult(Any.pack(result));
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Execute ServerWorkTask error, request id: {}, err msg: {}."
                        , request.getRequestId(), e.getMessage(), e);
            } else {
                log.warn("Execute ServerWorkTask error, request id: {}, err msg: {}."
                        , request.getRequestId(), e.getMessage());
            }
            builder.setErrMsg(String.format("%s: %s", e.getClass().getName(), e.getMessage()));
        }
        RpcMeta.ResponseMeta meta = builder.build();
        byte[] bytes = meta.toByteArray();
        ByteBuf byteBuf = Unpooled.buffer(bytes.length);
        byteBuf.writeBytes(bytes);
        ctx.channel().writeAndFlush(new RpcPacket(byteBuf))
                .addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("Send rpc response succeed, request id: {}.", meta.getRequestId());
                    } else {
                        log.warn("Send rpc response failed! request id: {}.", meta.getRequestId());
                    }
                });
    }
}
