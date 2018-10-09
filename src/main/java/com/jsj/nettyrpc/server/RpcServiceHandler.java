package com.jsj.nettyrpc.server;

import com.jsj.nettyrpc.common.bean.RpcRequest;
import com.jsj.nettyrpc.common.bean.RpcResponse;
import com.jsj.nettyrpc.common.constant.RpcResultEnum;
import com.jsj.nettyrpc.util.StringUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * RPC server（用于处理RPC Service请求）
 *
 * @author jsj
 * @date 2018-10-8
 */
public class RpcServiceHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcServiceHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        // 创建并初始化 RPC 响应对象
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        try {
            //调用服务，获取服务结果
            Object serviceResult = handle(request);
            //结果添加到响应
            response.setServiceResult(serviceResult);
            response.setRpcResultEnum(RpcResultEnum.SUCCESS);
        } catch (Exception e) {
            LOGGER.error("handle result failure", e);
            response.setRpcResultEnum(RpcResultEnum.FAIL);
        }
        // 写入 RPC 响应对象并关闭客户端连接
        //todo 可能有改动
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }

    /**
     * 根据请求调用已经注册的远程服务
     *
     * @param request
     * @return
     * @throws Exception
     */
    private Object handle(RpcRequest request) throws Exception {
        // 获取服务对象
        String serviceName = request.getInterfaceName();
        String serviceVersion = request.getServiceVersion();
        if (StringUtil.isNotEmpty(serviceVersion)) {
            serviceName += "-" + serviceVersion;
        }
        Object serviceBean = handlerMap.get(serviceName);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("can not find service bean by key: %s", serviceName));
        }
        // 获取反射调用所需的参数
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        // 执行反射调用
//        Method method = serviceClass.getMethod(methodName, parameterTypes);
//        method.setAccessible(true);
//        return method.invoke(serviceBean, parameters);
        // 使用 CGLib 执行反射调用
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }
}
