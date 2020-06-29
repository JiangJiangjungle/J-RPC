package com.jsj.rpc.protocol;

import com.jsj.rpc.exception.RpcCallException;
import com.jsj.rpc.protocol.standard.RpcProtocol;
import com.jsj.rpc.server.ServiceManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangshenjie
 */
public class ProtocolManager {
    private static final ProtocolManager INSTANCE = new ProtocolManager();

    private final Map<ProtocolType, Protocol> protocolMap = new HashMap<>();

    {
        protocolMap.put(ProtocolType.STANDARD, new RpcProtocol(ServiceManager.getInstance()));
    }

    private ProtocolManager() {
    }

    public static ProtocolManager getInstance() {
        return INSTANCE;
    }

    public Protocol getProtocol(ProtocolType protocolType) {
        Protocol protocol = protocolMap.get(protocolType);
        if (protocol == null) {
            throw new RpcCallException(String.format("No available protocol of %s yet", protocolType.getName()));
        }
        return protocol;
    }
}
