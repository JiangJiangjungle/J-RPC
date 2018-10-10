package com.jsj.nettyrpc.registry.impl;

import com.jsj.nettyrpc.registry.ServiceDiscovery;
import com.jsj.nettyrpc.registry.ServiceRegistry;
import com.jsj.nettyrpc.util.CollectionUtil;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于ZooKeeper的服务注册发现的接口实现
 *
 * @author jsj
 * @date 2018-10-10
 */
public class ZookeeperServiceCenter implements ServiceRegistry, ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceCenter.class);

    private final ZkClient zkClient;

    private final String zkAddress;

    public ZookeeperServiceCenter(String zkAddress) {
        this.zkAddress = zkAddress;
        // 创建 ZooKeeper 客户端
        zkClient = new ZkClient(zkAddress, ZooKeeperConfig.ZK_SESSION_TIMEOUT, ZooKeeperConfig.ZK_CONNECTION_TIMEOUT);
        // 创建 registry 节点（持久）
        if (!zkClient.exists(ZooKeeperConfig.ZK_REGISTRY_PATH)) {
            zkClient.createPersistent(ZooKeeperConfig.ZK_REGISTRY_PATH);
            LOGGER.debug("create registry node: {}", ZooKeeperConfig.ZK_REGISTRY_PATH);
        }
        LOGGER.debug("connect zookeeper");
    }

    @Override
    public String discover(String serviceName) {
        // 创建 ZooKeeper 客户端
        ZkClient zkClient = new ZkClient(zkAddress, ZooKeeperConfig.ZK_SESSION_TIMEOUT, ZooKeeperConfig.ZK_CONNECTION_TIMEOUT);
        LOGGER.debug("connect zookeeper");
        try {
            // 获取 service 节点
            String servicePath = ZooKeeperConfig.ZK_REGISTRY_PATH + "/" + serviceName;
            if (!zkClient.exists(servicePath)) {
                throw new RuntimeException(String.format("can not find any service node on path: %s", servicePath));
            }
            List<String> addressList = zkClient.getChildren(servicePath);
            if (CollectionUtil.isEmpty(addressList)) {
                throw new RuntimeException(String.format("can not find any address node on path: %s", servicePath));
            }
            // 获取 address 节点
            String address;
            int size = addressList.size();
            if (size == 1) {
                // 若只有一个地址，则获取该地址
                address = addressList.get(0);
                LOGGER.debug("get only address node: {}", address);
            } else {
                // 若存在多个地址，则随机获取一个地址
                //todo 可以考虑利用zooKeeper做更好的负载均衡
                address = addressList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("get random address node: {}", address);
            }
            // 获取 address 节点的值
            String addressPath = servicePath + "/" + address;
            return zkClient.readData(addressPath);
        } finally {
            zkClient.close();
        }
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        // 创建 registry 节点（持久）
        String registryPath = ZooKeeperConfig.ZK_REGISTRY_PATH;
        if (!zkClient.exists(registryPath)) {
            zkClient.createPersistent(registryPath);
            LOGGER.debug("create registry node: {}", registryPath);
        }
        // 创建 service 节点（持久）
        String servicePath = registryPath + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
            LOGGER.debug("create service node: {}", servicePath);
        }
        // 创建 address 节点（临时）
        String addressPath = servicePath + "/address-";
        String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
        LOGGER.debug("create address node: {}", addressNode);
    }
}
