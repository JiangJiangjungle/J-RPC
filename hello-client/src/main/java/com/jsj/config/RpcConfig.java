package com.jsj.config;

import com.jsj.rpc.client.RpcProxy;
import com.jsj.rpc.registry.ServiceDiscovery;
import com.jsj.rpc.registry.impl.ZookeeperDiscovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcConfig {

    private String host;

    @Bean
    public ServiceDiscovery initServiceDiscovery() {
        host = "119.23.204.78";
        String port = "2181";
        return new ZookeeperDiscovery(host + ":" + port);
    }

    @Bean
    public RpcProxy initRpcProxy(@Autowired ServiceDiscovery serviceDiscovery) {
        return new RpcProxy(serviceDiscovery);
    }
}
