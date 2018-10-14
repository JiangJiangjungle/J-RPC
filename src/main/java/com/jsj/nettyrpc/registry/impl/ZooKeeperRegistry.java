package com.jsj.nettyrpc.registry.impl;


import com.jsj.nettyrpc.registry.ServiceRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 ZooKeeper 的服务注册接口实现
 *
 * @author huangyong
 * @since 1.0.0
 */
public class ZooKeeperRegistry implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperRegistry.class);

    private final CuratorFramework client;

    public ZooKeeperRegistry(String zkAddress) {
        // 创建 ZooKeeper 客户端
        this.client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .sessionTimeoutMs(ZooKeeperConfig.ZK_SESSION_TIMEOUT)
                .connectionTimeoutMs(ZooKeeperConfig.ZK_CONNECTION_TIMEOUT)
                .retryPolicy(ZooKeeperConfig.RETRY_POLICY)
                .build();
        this.client.start();
        LOGGER.debug("connect zookeeper");
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        try {
            // 检查 registry 节点和 service 节点（持久）
            this.checkExists(serviceName);
            String servicePath = ZooKeeperConfig.ZK_REGISTRY_PATH + "/" + serviceName;
            // 创建 address 节点（临时）
            String addressPath = servicePath + "/address-";
            String addressNode = this.client.create()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(addressPath, serviceAddress.getBytes());
            LOGGER.debug("create address node: {}", addressNode);
        } catch (Exception e) {
            throw new RuntimeException("RPC server 注册时发生异常");
        }
    }

    /**
     * 检查 registry 节点和 service 节点是否已经创建
     *
     * @param serviceName
     */
    private void checkExists(String serviceName) {
        try {
            // 创建 registry 节点（持久）
            Stat stat = this.client.checkExists().forPath(ZooKeeperConfig.ZK_REGISTRY_PATH);
            if (stat == null) {
                this.client.create().withMode(CreateMode.PERSISTENT).forPath(ZooKeeperConfig.ZK_REGISTRY_PATH);
                LOGGER.debug("创建 registry 节点: {}", ZooKeeperConfig.ZK_REGISTRY_PATH);
            }
            // 创建 service 节点（持久）
            String servicePath = ZooKeeperConfig.ZK_REGISTRY_PATH + "/" + serviceName;
            stat = this.client.checkExists().forPath(servicePath);
            if (stat == null) {
                this.client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath);
                LOGGER.debug("创建 service 节点: {}", servicePath);
            }
        } catch (Exception e) {
            throw new RuntimeException("创建 registry 节点和 service 节点时发生异常");
        }
    }
}