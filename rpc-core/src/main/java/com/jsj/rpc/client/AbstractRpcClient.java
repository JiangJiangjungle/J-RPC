package com.jsj.rpc.client;

import com.jsj.rpc.ChannelInfo;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.client.channel.RpcChannel;
import com.jsj.rpc.client.instance.Endpoint;
import com.jsj.rpc.exception.RpcException;
import com.jsj.rpc.exception.RpcExceptionType;
import com.jsj.rpc.protocol.Packet;
import com.jsj.rpc.protocol.Protocol;
import com.jsj.rpc.protocol.Request;
import com.jsj.rpc.protocol.Response;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author jiangshenjie
 */
@Slf4j
@Getter
public abstract class AbstractRpcClient {
    protected final Endpoint endpoint;
    protected final RpcClientOptions clientOptions;
    protected Protocol protocol;
    protected RpcChannel rpcChannel;

    protected NioEventLoopGroup workerGroup;
    protected ScheduledThreadPoolExecutor scheduledThreadPool;
    protected ThreadPoolExecutor workerThreadPool;
    /**
     * 状态
     */
    protected AtomicBoolean isStop;

    public AbstractRpcClient(Endpoint endpoint, RpcClientOptions clientOptions) {
        this.endpoint = endpoint;
        this.clientOptions = clientOptions;
        init();
    }

    protected abstract void init();

    public void handleErrorResponse(RpcFuture<?> rpcFuture, Exception e) {
        Request request = rpcFuture.getRequest();
        Response response = protocol.createResponse();
        response.setRequestId(request.getRequestId());
        response.setRpcFuture(rpcFuture);
        response.setException(e);
        rpcFuture.handleResponse(response);
    }

    public <T> RpcFuture<T> sendRequest(Request request) {
        RpcFuture<T> rpcFuture = RpcFuture.createRpcFuture(request);
        Channel channel = null;
        ChannelInfo channelInfo = null;
        try {
            channel = rpcChannel.getChannel();
            channelInfo = ChannelInfo.getOrCreateClientChannelInfo(channel);
            channelInfo.addRpcFuture(rpcFuture);
            scheduleTimeoutTask(rpcFuture);
            Packet packet = request.transToPacket();
            boolean writeSucceed = channel.writeAndFlush(packet)
                    .await(clientOptions.getWriteTimeoutMillis(), TimeUnit.MILLISECONDS);
            if (!writeSucceed) {
                throw new RpcException(String.format("Write rpc request failed, request: %s.", request));
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

    protected void scheduleTimeoutTask(RpcFuture<?> rpcFuture) {
        scheduledThreadPool.schedule(
                () -> handleErrorResponse(rpcFuture, new RpcException(RpcExceptionType.TIMEOUT_EXCEPTION))
                , clientOptions.getRpcTaskTimeoutMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * 向Channel写入Request后，对Channel进行后置处理
     *
     * @param channel
     */
    protected abstract void processChannelAfterSendRequest(Channel channel);

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public void shutdown() {
        if (isStop.compareAndSet(false, true)) {
            //关闭连接
            rpcChannel.close();
            if (!clientOptions.isGlobalThreadPoolSharing()) {
                //优雅退出，释放 NIO 线程组
                workerGroup.shutdownGracefully().awaitUninterruptibly();
                //释放业务线程池
                workerThreadPool.shutdown();
            }
            log.info("rpc client shutdown.");
        }
    }
}
