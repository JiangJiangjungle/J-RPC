package com.jsj.rpc.server;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.jsj.rpc.protocol.Packet;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.Request;
import com.jsj.rpc.protocol.RpcMeta;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author jiangshenjie
 */
@Slf4j
@Getter
@Setter
public class ServerWorkTask implements Runnable {
    private Request request;
    private Protocol protocol;
    private ChannelHandlerContext ctx;

    public ServerWorkTask(Request request, Protocol protocol, ChannelHandlerContext ctx) {
        this.request = request;
        this.protocol = protocol;
        this.ctx = ctx;
    }

    @Override
    public void run() {
        RpcMeta.ResponseMeta responseMeta = executeRequest(request);
        Packet packet = protocol.createPacket(responseMeta.toByteArray());

        ctx.channel().writeAndFlush(packet).addListener(
                future -> {
                    if (future.isSuccess()) {
                        log.info("Send rpc response succeed, request id: {}.", responseMeta.getRequestId());
                    } else {
                        log.warn("Send rpc response failed! request id: {}.", responseMeta.getRequestId());
                    }
                }
        );
    }

    private RpcMeta.ResponseMeta executeRequest(Request request) {
        Message result = null;
        String errMsg = null;
        try {
            Method method = request.getMethod();
            //执行结果必须是com.google.protobuf.Message的子类
            result = (Message) method.invoke(request.getTarget(), request.getParams());
        } catch (Exception e) {
            log.warn("Execute ServerWorkTask error, request id: {}, err msg: {}."
                    , request.getRequestId(), e.getMessage(), e);
            errMsg = String.format("%s: %s", e.getClass().getName(), e.getMessage());
        }
        RpcMeta.ResponseMeta.Builder responseMetaBuilder = RpcMeta.ResponseMeta.newBuilder();
        responseMetaBuilder.setRequestId(request.getRequestId());
        if (result != null) {
            responseMetaBuilder.setResult(Any.pack(result));
        }
        if (errMsg != null) {
            responseMetaBuilder.setErrMsg(errMsg);
        }
        return responseMetaBuilder.build();
    }
}
