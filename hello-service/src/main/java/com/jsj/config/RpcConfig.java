package com.jsj.config;

import com.jsj.nettyrpc.registry.ServiceRegistry;
import com.jsj.nettyrpc.registry.impl.ZooKeeperServiceRegistry;
import com.jsj.nettyrpc.server.RpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Configuration
public class RpcConfig {

    private String host;

    ServiceRegistry serviceRegistry;

    RpcServer rpcServer;

    @Bean
    public ServiceRegistry initServiceRegistry() {
        host = "119.23.204.78";
        String port = "2181";
        System.out.println("initServiceRegistry");
        return new ZooKeeperServiceRegistry(host + ":" + port);
    }

    @Bean
    public RpcServer initRpcServer(@Autowired ServiceRegistry serviceRegistry) {
        host="127.0.0.1";
        String port = "8080";
        System.out.println("initRpcServer");
        return new RpcServer(host + ":" + port, serviceRegistry);
    }


}
