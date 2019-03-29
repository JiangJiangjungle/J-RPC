package com.jsj.config;

import com.jsj.rpc.codec.CodeStrategy;
import com.jsj.rpc.server.RpcServer;
import com.jsj.rpc.registry.ServiceRegistry;
import com.jsj.rpc.registry.impl.ZooKeeperRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcConfig {

    private String host;

    @Bean
    public ServiceRegistry initServiceRegistry() {
        host = "139.9.77.156";
        String port = "2181";
        return new ZooKeeperRegistry(host + ":" + port);
    }

    @Bean
    public RpcServer initRpcServer(@Autowired ServiceRegistry serviceRegistry) {
        host = "127.0.0.1";
        int port = 6666;
        return new RpcServer(host, port, serviceRegistry, CodeStrategy.JSON);
    }


}
