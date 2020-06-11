package com.jsj.rpc.server;

import com.jsj.rpc.protocol.ProtocolType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jiangshenjie
 * @date 2020/5/22
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class RpcServerOptions {
    private ProtocolType protocolType = ProtocolType.STANDARD;
    private boolean keepAlive = true;
    private boolean tcpNoDelay = true;
    /**
     * (s)
     */
    private int soLinger = 5;
    private int backlog = 1024;
    /**
     * buffer size (Byte)
     */
    private int receiveBufferSize = 1024 * 64;
    private int sendBufferSize = 1024 * 64;

    private int acceptThreadNumber = 1;
    /**
     * io threads, default use Netty default value
     */
    private int ioThreadNumber = Runtime.getRuntime().availableProcessors();
    private int workerThreadNumber = Runtime.getRuntime().availableProcessors();
    /**
     * blocking queue size of worker thread pool
     */
    private int workerThreadPoolQueueSize = 1024;

    /**
     * channel idle time (ms)
     */
    private long readIdleTime = 60 * 1000L;
    private long writeIdleTime = 60 * 1000L;
    private long keepAliveTime = 5 * 1000L;


}
