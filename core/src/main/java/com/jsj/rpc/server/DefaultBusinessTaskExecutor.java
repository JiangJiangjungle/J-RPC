package com.jsj.rpc.server;

import com.jsj.rpc.RpcRequest;
import com.jsj.rpc.RpcResponse;
import com.jsj.rpc.RpcStateCode;
import com.jsj.rpc.common.NamedThreadFactory;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangshenjie
 */
public class DefaultBusinessTaskExecutor implements BusinessTaskExecutor {
    /**
     * 用户业务线程池，用于处理实际rpc业务
     */
    private static ExecutorService threadPool = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Runtime.getRuntime().availableProcessors() * 2, 60L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1000), new NamedThreadFactory());

    /**
     * 本服务端注册的服务接口实例
     */
    private Map<String, Object> registeredServices;

    public DefaultBusinessTaskExecutor(Map<String, Object> registeredServices) {
        this.registeredServices = registeredServices;
    }

    @Override
    public void execute(RpcRequest request, TaskCallbackHandler<RpcResponse> handler) {
        Runnable task = () -> {
            // 创建并初始化 RPC 响应对象
            RpcResponse response = new RpcResponse();
            response.setRequestId(request.getRequestId());
            try {
                //调用服务，获取服务结果
                Object serviceResult = this.handle(request);
                //结果添加到响应
                response.setServiceResult(serviceResult);
            } catch (Exception e) {
                e.printStackTrace();
                response.setErrorMsg(String.format("ErrorCode: %s, state: %s, cause: %s", RpcStateCode.FAIL.getCode(),
                        RpcStateCode.FAIL.getValue(), e.getMessage()));
            }
            //在业务线程中执行回调函数
            handler.callback(response);
        };
        threadPool.execute(task);
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
        return this.invoke(serviceBean, methodName, parameterTypes, parameters);
    }

    /**
     * 获取服务实例对象
     *
     * @param serviceName service接口名称
     * @return
     */
    private Object getServiceBean(String serviceName) {
        Object serviceBean = this.registeredServices.get(serviceName);
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
    private Object invoke(Object serviceBean, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws InvocationTargetException {
        // 获取反射调用所需的参数
        Class<?> serviceClass = serviceBean.getClass();
        // 使用 CGLib 执行反射调用
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }
}
