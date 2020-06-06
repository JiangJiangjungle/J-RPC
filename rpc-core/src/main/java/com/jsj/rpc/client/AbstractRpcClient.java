package com.jsj.rpc.client;

import com.jsj.rpc.ChannelInfo;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.exception.RpcException;
import com.jsj.rpc.protocol.Packet;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.Request;
import com.jsj.rpc.protocol.Response;
import io.netty.channel.Channel;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangshenjie
 */
public abstract class AbstractRpcClient {
    Protocol protocol;
    ScheduledThreadPoolExecutor scheduledThreadPool;

    public AbstractRpcClient() {
    }

    public void handleResponse(Response response) {
        RpcFuture<?> rpcFuture = response.getRpcFuture();
        rpcFuture.handleResponse(response);
    }

    public void handleErrorResponse(RpcFuture<?> rpcFuture, Exception e) {
        Request request = rpcFuture.getRequest();
        Response response = protocol.createResponse();
        response.setRequestId(request.getRequestId());
        response.setRpcFuture(rpcFuture);
        response.setException(e);
        handleResponse(response);
    }

    public <T> RpcFuture<T> sendRequest(Request request) {
        RpcFuture<T> rpcFuture = RpcFuture.createRpcFuture(request);
        Channel channel = null;
        ChannelInfo channelInfo = null;
        try {
            channel = selectChannel(request);
            channelInfo = ChannelInfo.getOrCreateClientChannelInfo(channel);
            channelInfo.addRpcFuture(rpcFuture);
            scheduleTimeoutTask(rpcFuture, channel);
            Packet packet = request.transToPacket();
            boolean writeSucceed = channel.writeAndFlush(packet)
                    .await(request.getWriteTimeoutMillis(), TimeUnit.MILLISECONDS);
            if (!writeSucceed) {
                throw new RpcException(String.format("Send rpc request failed, request: %s.", request));
            }
        } catch (Exception e) {
            handleErrorResponse(rpcFuture, e);
            if (channel != null && channelInfo != null) {
                channelInfo.getAndRemoveRpcFuture(request.getRequestId());
            }
        } finally {
            if (channel != null) {
                processChannelAfterSendRequest(channel);
            }
        }
        return rpcFuture;
    }

    protected void scheduleTimeoutTask(RpcFuture<?> rpcFuture, Channel channel) {
        Request request = rpcFuture.getRequest();
        scheduledThreadPool.schedule(
                () -> {
                    long requestId = request.getRequestId();
                    long elapseTime = System.currentTimeMillis() - rpcFuture.getStartTime();
                    String errMsg = String.format(
                            "request timeout, requestId: %d, remote addr: %s, elapseTime: %dms."
                            , requestId, channel.remoteAddress(), elapseTime);
                    handleErrorResponse(rpcFuture, new RpcException(errMsg));
                }
                , request.getTaskTimeoutMills(), TimeUnit.MILLISECONDS);
    }

    /**
     * 向Channel写入Request后，对Channel进行后置处理
     *
     * @param channel
     */
    protected abstract void processChannelAfterSendRequest(Channel channel);

    /**
     * 获取一个Channel
     *
     * @param request
     */
    protected abstract Channel selectChannel(Request request) throws Exception;

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public ScheduledThreadPoolExecutor getScheduledThreadPool() {
        return scheduledThreadPool;
    }

    public void setScheduledThreadPool(ScheduledThreadPoolExecutor scheduledThreadPool) {
        this.scheduledThreadPool = scheduledThreadPool;
    }
}
