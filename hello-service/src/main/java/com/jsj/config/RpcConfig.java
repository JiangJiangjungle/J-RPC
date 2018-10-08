package com.jsj.config;

import com.jsj.nettyrpc.registry.ServiceRegistry;
import com.jsj.nettyrpc.registry.impl.ZooKeeperServiceRegistry;
import com.jsj.nettyrpc.server.RpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RpcConfig {

    private String host;

    ServiceRegistry serviceRegistry;

    RpcServer rpcServer;

    @PostConstruct
    @Bean
    public ServiceRegistry initServiceRegistry() {
        host = "119.23.204.78";
        String port = "2181";
        return new ZooKeeperServiceRegistry(host + ":" + port);
    }

    @PostConstruct
    @Bean
    public RpcServer initRpcServer(@Autowired ServiceRegistry serviceRegistry) {
        String port = "8080";
        return new RpcServer(host + ":" + port, serviceRegistry);
    }


}
