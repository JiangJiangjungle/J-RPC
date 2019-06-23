package com.jsj.sample.provider.config;

import com.jsj.rpc.registry.ServiceRegistry;
import com.jsj.rpc.registry.impl.ZooKeeperRegistry;
import com.jsj.rpc.server.RpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcConfiguration {
    @Value("${rpc.registry.ip}")
    private String registryIP;
    @Value("${rpc.registry.port}")
    private int registryPort;
    @Value("${rpc.provider.ip}")
    private String serviceIP;
    @Value("${rpc.provider.port}")
    private int servicePort;

    @Bean
    public ServiceRegistry serviceRegistry() {
        return new ZooKeeperRegistry(registryIP, registryPort);
    }

    @Bean
    public RpcServer initRpcServer(@Autowired ServiceRegistry serviceRegistry) {
        return new RpcServer(serviceIP, servicePort, serviceRegistry);
    }
}
