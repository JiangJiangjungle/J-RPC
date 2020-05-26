package com.jsj.rpc.protocol;

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
        if (protocolType == ProtocolType.RPC_PROTOCOL) {
            return new RpcProtocol();
        }
        return null;
    }
}
