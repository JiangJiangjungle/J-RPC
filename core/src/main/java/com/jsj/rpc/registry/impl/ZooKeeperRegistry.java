package com.jsj.rpc.registry.impl;


import com.jsj.rpc.exception.RpcServiceNotFoundException;
import com.jsj.rpc.registry.ServiceDiscovery;
import com.jsj.rpc.registry.ServiceRegistry;
import com.jsj.rpc.util.CollectionUtil;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于 ZooKeeper 的服务中心实现
 *
 * @author jsj
 * @date 2018-10-14
 */
public class ZooKeeperRegistry implements ServiceRegistry, ServiceDiscovery {
    /**
     * 连接最大重试次数
     */
    private final static int MAX_RETRYS = 3;
    private final static int BASE_SLEEP_TIMEOUT = 1000;
    /**
     * ZooKeeper连接的重试策略
     */
    private final static RetryPolicy RETRY_POLICY = new ExponentialBackoffRetry(BASE_SLEEP_TIMEOUT, MAX_RETRYS);
    /**
     * 会话超时参数
     */
    private final static int ZK_SESSION_TIMEOUT = 5000;
    /**
     * 连接超时参数
     */
    private final static int ZK_CONNECTION_TIMEOUT = 5000;
    /**
     * rpc注册根节点的命名空间
     */
    private final static String ZK_REGISTRY_PATH = "/rpc";
    private static final String SEPARATOR = ":";
    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperRegistry.class);
    private final String address;

    public ZooKeeperRegistry(String ip, int port) {
        this.address = ip + SEPARATOR + port;
    }

    private CuratorFramework createCuratorFramework() {
        // 创建 ZooKeeper 客户端
        return CuratorFrameworkFactory.builder()
                .connectString(address)
                .sessionTimeoutMs(ZK_SESSION_TIMEOUT)
                .connectionTimeoutMs(ZK_CONNECTION_TIMEOUT)
                .retryPolicy(RETRY_POLICY)
                .build();
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        CuratorFramework client = createCuratorFramework();
        try {
            client.start();
            LOGGER.info("Zookeeper 已连接！");
            // 检查 registry 节点和 service 节点（持久）
            this.checkNodeExists(client, serviceName);
            String servicePath = ZK_REGISTRY_PATH + "/" + serviceName;
            // 创建 address 节点（临时），只有停止服务后才能断开连接
            String addressPath = servicePath + "/address-";
            String addressNode = client.create()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(addressPath, serviceAddress.getBytes());
            LOGGER.info("创建 address 节点: {}", addressNode);
        } catch (Exception e) {
            throw new RuntimeException("RPC server 注册时发生异常");
        }
    }

    @Override
    public String discover(String serviceName) throws RpcServiceNotFoundException {
        CuratorFramework client = createCuratorFramework();
        try {
            client.start();
            LOGGER.info("Zookeeper 已连接！");
            // 检查 registry 节点和 service 节点（持久）
            this.checkNodeExists(client, serviceName);
            // 获取 service 节点
            String servicePath = ZK_REGISTRY_PATH + "/" + serviceName;
            // 获取所有 address 节点
            List<String> addressList = client.getChildren().forPath(servicePath);
            if (CollectionUtil.isEmpty(addressList)) {
                throw new RpcServiceNotFoundException(String.format("未发现任何节点注册信息！: %s", servicePath));
            }
            //选取 serviceAddress 节点
            String serviceAddress = this.chooseServiceAddress(addressList);
            // 获取 address 节点的值
            String addressPath = servicePath + "/" + serviceAddress;
            return new String(client.getData().forPath(addressPath));
        } catch (RpcServiceNotFoundException r) {
            throw r;
        } catch (Exception e) {
            throw new RpcServiceNotFoundException(e.getMessage());
        } finally {
            //获取节点信息后，关闭连接
            client.close();
        }
    }

    /**
     * 检查 registry 节点和 service 节点是否已经创建
     *
     * @param serviceName
     */
    private void checkNodeExists(CuratorFramework client, String serviceName) throws Exception {
        // 检查 registry 节点（持久）
        checkRegistryNodeExists(client);
        // 检查 service 节点（持久）
        String servicePath = ZK_REGISTRY_PATH + "/" + serviceName;
        Stat stat = client.checkExists().forPath(servicePath);
        if (stat == null) {
            client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath);
            LOGGER.info("创建 service 节点: {}", servicePath);
        }
    }

    /**
     * 选取 serviceAddress 节点
     *
     * @param addressList serviceAddress 节点列表
     * @return
     */
    private String chooseServiceAddress(List<String> addressList) {
        if (CollectionUtil.isEmpty(addressList)) {
            return null;
        }
        String address;
        int size = addressList.size();
        if (size == 1) {
            // 若只有一个地址，则获取该地址
            address = addressList.get(0);
            LOGGER.debug("选取任意唯一节点: {}", address);
        } else {
            //todo 若存在多个地址，则随机获取一个地址，可以考虑利用zooKeeper做更好的负载均衡
            address = addressList.get(ThreadLocalRandom.current().nextInt(size));
            LOGGER.debug("选取任意一个节点: {}", address);
        }
        return address;
    }

    /**
     * 检查 registry 节点是否已经创建
     *
     * @param client
     */
    private void checkRegistryNodeExists(CuratorFramework client) {
        try {
            // 创建 registry 节点（持久）
            Stat stat = client.checkExists().forPath(ZK_REGISTRY_PATH);
            if (stat == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(ZK_REGISTRY_PATH);
                LOGGER.info("创建 registry 节点: {}", ZK_REGISTRY_PATH);
            }
        } catch (Exception e) {
            LOGGER.debug("创建 registry 节点时发生异常");
            throw new RuntimeException("创建 registry 节点时发生异常");
        }
    }
}