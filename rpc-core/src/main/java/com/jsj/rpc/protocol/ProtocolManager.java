package com.jsj.rpc.protocol;

import com.jsj.rpc.protocol.standard.RpcProtocol;

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
            return new RpcProtocol();
        }
        return null;
    }
}
