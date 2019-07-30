package com.jsj.rpc.server;

import com.jsj.rpc.RpcRequest;
import com.jsj.rpc.common.serializer.SerializerTypeEnum;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author jiangshenjie
 */
public interface TaskExecutor {

    /**
     * 异步执行业务
     *
     * @param ctx
     * @param rpcRequest
     * @param serializationType
     */
    void execute(ChannelHandlerContext ctx, RpcRequest rpcRequest, SerializerTypeEnum serializationType);

    /**
     * 添加服务实例
     *
     * @param serviceName
     * @param instance
     */
    void addInstance(String serviceName, Object instance);
}
