package com.jsj.rpc.server;

import com.jsj.rpc.exception.RpcException;
import com.jsj.rpc.protocol.Packet;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.Request;
import com.jsj.rpc.protocol.Response;
import io.netty.channel.Channel;
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
    private Channel channel;

    public ServerWorkTask(Request request, Protocol protocol, Channel channel) {
        this.request = request;
        this.protocol = protocol;
        this.channel = channel;
    }

    @Override
    public void run() {
        Response response = executeRequest(request);
        Packet packet = response.transToPacket();
        channel.writeAndFlush(packet).addListener(
                future -> {
                    if (future.isSuccess()) {
                        log.debug("Send rpc response: {} succeed.", response);
                    } else {
                        log.warn("Send rpc response: {} failed!", response);
                    }
                }
        );
    }

    private Response executeRequest(Request request) {
        Object result = null;
        String errMsg = null;
        try {
            Method method = request.getMethod();
            result = method.invoke(request.getTarget(), request.getParams());
        } catch (Exception e) {
            log.warn("Execute ServerWorkTask error, request id: {}, err msg: {}."
                    , request.getRequestId(), e.getMessage(), e);
            errMsg = String.format("%s: %s", e.getClass().getName(), e.getMessage());
        }
        Response response = protocol.createResponse();
        response.setRequestId(request.getRequestId());
        if (result != null) {
            response.setResult(result);
        }
        if (errMsg != null) {
            response.setException(new RpcException(errMsg));
        }
        return response;
    }
}
