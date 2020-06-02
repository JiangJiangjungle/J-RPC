package com.jsj.rpc.client;

import com.jsj.rpc.protocol.ProtocolType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author jiangshenjie
 */
@Getter
@Setter
@NoArgsConstructor
public class RpcClientOptions {
    private ProtocolType protocolType = ProtocolType.STANDARD;

    private int connectTimeoutMillis = 1500;

    /**
     * The keep alive
     */
    private boolean keepAlive = true;
    private boolean reuseAddr = true;
    private boolean tcpNoDelay = true;
    private int soLinger = 5;

    /**
     * read/write timeout
     */
    private int readTimeoutMillis = 1000;
    private int writeTimeoutMillis = 1000;

    /**
     * receive buffer size
     */
    private int receiveBufferSize = 1024 * 64;
    /**
     * send buffer size
     */
    private int sendBufferSize = 1024 * 64;
    /**
     * keep alive time in ms
     */
    private long keepAliveTime = 5 * 60 * 1000L;
    /**
     * max channel number with one endpoint
     */
    private int maxChannelNumber = 3;
    /**
     * io threads, default use Netty default value
     */
    private int ioThreadNumber = Runtime.getRuntime().availableProcessors();
    /**
     * threads used for deserialize com.jsj.rpc response and execute the callback
     */
    private int workerThreadNumber = Runtime.getRuntime().availableProcessors();
    /**
     * blocking queue size of worker thread pool
     */
    private int workerThreadPoolQueueSize = 1024;

    /**
     * share worker thread poll and event thread pool between multi RpcClients
     */
    private boolean globalThreadPoolSharing = true;
}
