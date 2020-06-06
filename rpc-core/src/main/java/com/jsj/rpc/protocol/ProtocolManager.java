package com.jsj.rpc.protocol;

import com.jsj.rpc.protocol.standard.RpcProtocol;
import com.jsj.rpc.server.ServiceManager;

/**
 * @author jiangshenjie
 */
public class ProtocolManager {
    private static ProtocolManager manager = new ProtocolManager();

    private ProtocolManager() {
    }

    public static ProtocolManager getInstance() {
        return manager;
    }

    public Protocol getProtocol(ProtocolType protocolType) {
        if (protocolType == ProtocolType.STANDARD) {
            return new RpcProtocol(ServiceManager.getInstance());
        } else if (protocolType == ProtocolType.HTTP) {
            throw new RuntimeException("Not support yet.");
        }
        return null;
    }
}
