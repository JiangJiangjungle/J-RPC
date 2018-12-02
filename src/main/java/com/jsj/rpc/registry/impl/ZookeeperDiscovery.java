package com.jsj.rpc.registry.impl;

import com.jsj.rpc.exception.RpcServiceNotFoundException;
import com.jsj.rpc.registry.ServiceDiscovery;
import com.jsj.rpc.util.CollectionUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于 ZooKeeper 的服务发现接口实现
 *
 * @author jsj
 * @date 2018-10-14
 */
public class ZookeeperDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperDiscovery.class);

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
            LOGGER.info("Zookeeper 已连接！");
            // 获取 service 节点
            String servicePath = ZooKeeperConfig.ZK_REGISTRY_PATH + "/" + serviceName;
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
    private void checkExists(CuratorFramework client) {
        try {
            // 创建 registry 节点（持久）
            Stat stat = client.checkExists().forPath(ZooKeeperConfig.ZK_REGISTRY_PATH);
            if (stat == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(ZooKeeperConfig.ZK_REGISTRY_PATH);
                LOGGER.info("创建 registry 节点: {}", ZooKeeperConfig.ZK_REGISTRY_PATH);
            }
        } catch (Exception e) {
            LOGGER.debug("创建 registry 节点和 service 节点时发生异常");
            throw new RuntimeException("创建 registry 节点和 service 节点时发生异常");
        }
    }
}
