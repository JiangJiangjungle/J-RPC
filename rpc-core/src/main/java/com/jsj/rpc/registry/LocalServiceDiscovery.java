package com.jsj.rpc.registry;

import com.jsj.rpc.client.Endpoint;

import java.util.*;

/**
 * @author jiangshenjie
 */
public class LocalServiceDiscovery implements ServiceDiscovery {
    private Random random = new Random();
    private Map<String, Set<Endpoint>> serviceEndpoints = new HashMap<>(16);

    @Override
    public Endpoint discover(String serviceName) throws RpcServiceNotFoundException {
        Set<Endpoint> endpoints = serviceEndpoints.get(serviceName);
        if (endpoints == null || endpoints.size() == 0) {
            return null;
        }
        //随机选取服务实例
        Endpoint endpoint = null;
        int i = 0;
        int selected = random.nextInt(endpoints.size());
        for (Endpoint each : endpoints) {
            if (i++ != selected) {
                continue;
            }
            endpoint = each;
            break;
        }
        return endpoint;
    }

    public void addEndpoint(String serviceName, Endpoint endpoint) {
        if (!serviceEndpoints.containsKey(serviceName)) {
            serviceEndpoints.put(serviceName, new HashSet<>(8));
        }
        serviceEndpoints.get(serviceName).add(endpoint);
    }
}
