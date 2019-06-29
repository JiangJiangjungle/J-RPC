package com.jsj.rpc.task;

import com.jsj.rpc.RpcStateCode;
import com.jsj.rpc.codec.serializer.SerializerTypeEnum;
import com.jsj.rpc.protocol.*;
import com.jsj.rpc.server.RpcServer;
import com.jsj.rpc.util.MessageUtil;
import io.netty.channel.ChannelHandlerContext;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * rpc 调用任务，在业务线程池中执行
 *
 * @author jsj
 * @date 2018-12-3
 */
public class RpcTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcTask.class);

    private ChannelHandlerContext ctx;
    private RpcRequest request;
    private SerializerTypeEnum serializationType;

    public RpcTask(ChannelHandlerContext ctx, RpcRequest request, SerializerTypeEnum serializationType) {
        this.ctx = ctx;
        this.request = request;
        this.serializationType = serializationType;
    }

    @Override
    public void run() {
        // 创建并初始化 RPC 响应对象
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        try {
            //调用服务，获取服务结果
            Object serviceResult = this.handle(request);
            //结果添加到响应
            response.setServiceResult(serviceResult);
        } catch (Exception e) {
            response.setErrorMsg(String.format("errorCode: %s, state: %s, cause: %s", RpcStateCode.FAIL.getCode(),
                    RpcStateCode.FAIL.getValue(), e.getMessage()));
        }
        //根据 RPC 响应对象封装Message
        Message message = MessageUtil.createMessage(MessageTypeEnum.RPC_RESPONSE, serializationType, response);
        // 写入 Message
        ctx.writeAndFlush(message);
        LOGGER.info("执行完毕！{} ", request.toString());
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
     * @param serviceName service接口名称
     * @return
     */
    private Object getServiceBean(String serviceName) {
        Object serviceBean = RpcServer.serviceInstanceMap.get(serviceName);
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
        // 使用 CGLib 执行反射调用
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }
}
