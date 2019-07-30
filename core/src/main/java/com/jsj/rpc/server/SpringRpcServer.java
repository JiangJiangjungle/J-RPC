package com.jsj.rpc.server;

import com.jsj.rpc.RpcService;
import com.jsj.rpc.config.DefaultServerConfiguration;
import com.jsj.rpc.registry.ServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

/**
 * 基于Spring的服务端启动器，自动扫描带@RpcService注解的服务实例
 */
public class SpringRpcServer extends DefaultRpcServer implements ApplicationListener<ContextRefreshedEvent> {

    public SpringRpcServer(String ip, int port, ServiceRegistry serviceRegistry) {
        super(ip, port, serviceRegistry);
    }

    public SpringRpcServer(String ip, int port, ServiceRegistry serviceRegistry, DefaultServerConfiguration configuration) {
        super(ip, port, serviceRegistry, configuration);
    }


    /**
     * 添加监听，用于扫描服务并启动server
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        //root application context 保证只执行一次
        if (applicationContext.getParent() == null) {
            LOGGER.info("root application context 调用了onApplicationEvent！");
            //扫描带有 RpcService 注解的类并初始化 handlerMap 对象
            Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
            //注册
            this.registerAllService(serviceBeanMap);
            //启动server
            this.start();
        }
    }
}
