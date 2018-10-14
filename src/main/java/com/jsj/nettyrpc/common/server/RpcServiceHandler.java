package com.jsj.nettyrpc.common.server;

import com.jsj.nettyrpc.common.RpcRequest;
import com.jsj.nettyrpc.common.RpcResponse;
import com.jsj.nettyrpc.common.RpcStateCode;
import com.jsj.nettyrpc.util.StringUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
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
            Object serviceResult = this.handle(request);
            //结果添加到响应
            response.setServiceResult(serviceResult);
        } catch (Exception e) {
            LOGGER.error("handle result failure", e);
            response.setErrorMsg(String.format("errorCode: %s, state: %s, cause: %s", RpcStateCode.FAIL.getCode(),
                    RpcStateCode.FAIL.getValue(), e.getMessage()));
        }
        // 写入 RPC 响应对象
        ctx.writeAndFlush(response).sync();
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
        // 获取服务实例对象
        String serviceName = request.getInterfaceName();
        Object serviceBean = this.getServiceBean(serviceName);
        //利用反射调用服务
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        return this.invokeByReflect(serviceBean, methodName, parameterTypes, parameters);
    }

    /**
     * 获取服务实例对象
     *
     * @param serviceName    service接口名称
     * @return
     */
    private Object getServiceBean(String serviceName) {
        Object serviceBean = handlerMap.get(serviceName);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("can not find service bean by key: %s", serviceName));
        }
        return serviceBean;
    }


    /**
     * 利用反射机制调用服务，并返回结果(可以选择用JDK自带的反射机制，或者cglib提供的反射方法)
     *
     * @param serviceBean    service实例对象
     * @param methodName     调用方法名
     * @param parameterTypes 参数类型
     * @param parameters     参数值
     * @return
     * @throws InvocationTargetException
     */
    private Object invokeByReflect(Object serviceBean, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws InvocationTargetException {
        // 获取反射调用所需的参数
        Class<?> serviceClass = serviceBean.getClass();
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
