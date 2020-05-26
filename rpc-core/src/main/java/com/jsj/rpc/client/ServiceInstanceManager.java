package com.jsj.rpc.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Client的本地服务实例缓存
 *
 * @author jiangshenjie
 */
@Slf4j
public class ServiceInstanceManager {
    private static final Random RANDOM = new Random();
    private Map<Endpoint, Channel> channels = new HashMap<>(16);
    private Map<String, Set<Endpoint>> serviceEndpoints = new HashMap<>(16);
    private Map<Endpoint, Set<String>> serviceNames = new HashMap<>(16);
    private Lock lock = new ReentrantLock();

    public ServiceInstanceManager() {
    }

    /**
     * 增加服务实例
     *
     * @param serviceName
     * @param channel
     */
    public void addInstance(String serviceName, Channel channel) {
        Endpoint endpoint = parseAddress(channel);
        lock.lock();
        try {
            if (!serviceEndpoints.containsKey(serviceName)) {
                serviceEndpoints.put(serviceName, new HashSet<>(8));
            }
            if (!serviceNames.containsKey(endpoint)) {
                serviceNames.put(endpoint, new HashSet<>(8));
            }
            serviceNames.get(endpoint).add(serviceName);
            serviceEndpoints.get(serviceName).add(endpoint);
            channels.put(endpoint, channel);
        } finally {
            lock.unlock();
        }
        log.info("Add service instance: {}, channel: {}.", serviceName, endpoint.toString());
    }

    /**
     * 根据serviceName获取服务节点对应的Channel
     *
     * @param serviceName
     * @return
     */
    public Channel selectInstance(String serviceName) {
        if (!serviceEndpoints.containsKey(serviceName)) {
            return null;
        }
        Channel channel = null;
        Set<Endpoint> endpoints = serviceEndpoints.get(serviceName);
        //随机选取服务实例
        int i = 0;
        int selected = RANDOM.nextInt(endpoints.size());
        for (Endpoint endpoint : endpoints) {
            if (i++ != selected) {
                continue;
            }
            channel = channels.get(endpoint);
            break;
        }
        return channel;
    }

    /**
     * 删除服务实例
     *
     * @param channel
     */
    public void removeInstance(Channel channel) {
        removeInstance(parseAddress(channel));
    }

    public void removeInstance(Endpoint endpoint) {
        if (!serviceNames.containsKey(endpoint)) {
            return;
        }
        lock.lock();
        try {
            Set<String> registeredServices = serviceNames.get(endpoint);
            for (String serviceName : registeredServices) {
                Set<Endpoint> endpoints = serviceEndpoints.get(serviceName);
                if (endpoints == null) {
                    continue;
                }
                endpoints.remove(endpoint);
                channels.remove(endpoint);
            }
            serviceNames.remove(endpoint);
        } finally {
            lock.unlock();
        }
        log.info("Remove channel: {}.", endpoint.toString());
    }

    public void close() {
        lock.lock();
        try {
            for (Map.Entry<Endpoint, Channel> entry : channels.entrySet()) {
                Channel channel = entry.getValue();
                if (channel.isActive()) {
                    channel.close().awaitUninterruptibly();
                }
            }
        } finally {
            lock.unlock();
        }
        log.info("Close all channels.");
    }

    private Endpoint parseAddress(Channel channel) {
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        String ip = address.getAddress().getHostAddress();
        int port = address.getPort();
        return new Endpoint(ip, port);
    }

}
