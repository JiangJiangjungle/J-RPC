package com.jsj.nettyrpc.registry.impl;

import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ZooKeeperConfig {

    public final static int MAX_RETRYS = 3;
    public final static int BASE_SLEEP_TIMEOUT = 1000;

    public final static RetryPolicy RETRY_POLICY = new ExponentialBackoffRetry(ZooKeeperConfig.BASE_SLEEP_TIMEOUT, ZooKeeperConfig.MAX_RETRYS);

    public final static int ZK_SESSION_TIMEOUT = 5000;
    public final static int ZK_CONNECTION_TIMEOUT = 5000;

    public final static String ZK_REGISTRY_PATH = "/rpc";
}
