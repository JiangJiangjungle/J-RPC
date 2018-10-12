package com.jsj.nettyrpc.registry.impl;

import com.jsj.nettyrpc.exception.RpcServiceNotFoundException;
import com.jsj.nettyrpc.registry.ServiceDiscovery;
import com.jsj.nettyrpc.util.CollectionUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ZookeeperDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceCenter.class);

    private String zkAddress;

    public ZookeeperDiscovery(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    @Override
    public String discover(String serviceName) throws RpcServiceNotFoundException {
        // 创建 ZooKeeper 客户端
        CuratorFramework client =
                CuratorFrameworkFactory.builder()
                        .connectString(zkAddress)
                        .sessionTimeoutMs(ZooKeeperConfig.ZK_SESSION_TIMEOUT)
                        .connectionTimeoutMs(ZooKeeperConfig.ZK_CONNECTION_TIMEOUT)
                        .retryPolicy(ZooKeeperConfig.RETRY_POLICY)
                        .build();
        client.start();
        try {
            //检查 registry 节点是否存在，若不存在则创建该节点
            this.checkExists(client);
            LOGGER.debug("connect zookeeper");
            // 获取 service 节点
            String servicePath = ZooKeeperConfig.ZK_REGISTRY_PATH + "/" + serviceName;
            // 获取所有 address 节点
            List<String> addressList = client.getChildren().forPath(servicePath);
            if (CollectionUtil.isEmpty(addressList)) {
                throw new RpcServiceNotFoundException(String.format("can not find any address node on path: %s", servicePath));
            }
            //选取 serviceAddress 节点
            String serviceAddress = this.chooseServiceAddress(addressList);
            // 获取 address 节点的值
            String addressPath = servicePath + "/" + serviceAddress;
            return new String(client.getData().forPath(addressPath));
        } catch (RpcServiceNotFoundException r) {
            throw r;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            //关闭连接
            client.close();
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
            LOGGER.debug("get only address node: {}", address);
        } else {
            // 若存在多个地址，则随机获取一个地址
            //todo 可以考虑利用zooKeeper做更好的负载均衡
            address = addressList.get(ThreadLocalRandom.current().nextInt(size));
            LOGGER.debug("get random address node: {}", address);
        }
        return address;
    }

    /**
     * 检查 registry 节点是否已经创建
     *
     * @param client
     */
    private void checkExists(CuratorFramework client) {
        try {
            // 创建 registry 节点（持久）
            Stat stat = client.checkExists().forPath(ZooKeeperConfig.ZK_REGISTRY_PATH);
            if (stat == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(ZooKeeperConfig.ZK_REGISTRY_PATH);
                LOGGER.debug("创建 registry 节点: {}", ZooKeeperConfig.ZK_REGISTRY_PATH);
            }
        } catch (Exception e) {
            throw new RuntimeException("创建 registry 节点和 service 节点时发生异常");
        }
    }
}
