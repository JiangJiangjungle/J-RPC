package com.jsj.rpc.registry.impl;

import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * ZooKeeper连接配置
 *
 * @author jsj
 * @date 2018-10-14
 */
public class ZooKeeperConfig {

    /**
     * 连接最大重试次数
     */
    public final static int MAX_RETRYS = 3;
    public final static int BASE_SLEEP_TIMEOUT = 1000;
    /**
     * ZooKeeper连接的重试策略
     */
    public final static RetryPolicy RETRY_POLICY = new ExponentialBackoffRetry(ZooKeeperConfig.BASE_SLEEP_TIMEOUT,
            ZooKeeperConfig.MAX_RETRYS);

    /**
     * 会话超时参数
     */
    public final static int ZK_SESSION_TIMEOUT = 5000;
    /**
     * 连接超时参数
     */
    public final static int ZK_CONNECTION_TIMEOUT = 5000;
    /**
     * rpc注册根节点的命名空间
     */
    public final static String ZK_REGISTRY_PATH = "/rpc";
}
