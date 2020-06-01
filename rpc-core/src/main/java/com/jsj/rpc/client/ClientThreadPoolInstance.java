package com.jsj.rpc.client;

import com.jsj.rpc.util.NamedThreadFactory;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jiangshenjie
 */
public class ClientThreadPoolInstance {
    private static ThreadPoolExecutor workThreadPool;
    private static String workThreadPoolName = "rpc-client-work-thread";
    private static Lock workThreadPoolLock = new ReentrantLock();

    private static NioEventLoopGroup ioEventLoopGroup;
    private static String ioEventLoopGroupName = "rpc-client-io-thread";
    private static Lock ioEventLoopGroupLock = new ReentrantLock();

    public static ThreadPoolExecutor getOrCreateWorkThreadPool(int threadNumber, int blockingQueueSize) {
        if (workThreadPool == null) {
            workThreadPoolLock.lock();
            try {
                if (workThreadPool == null) {
                    workThreadPool = new ThreadPoolExecutor(threadNumber
                            , threadNumber, 0L, TimeUnit.MILLISECONDS
                            , new LinkedBlockingDeque<>(blockingQueueSize)
                            , new NamedThreadFactory(workThreadPoolName, false));
                }
            } finally {
                workThreadPoolLock.unlock();
            }
        }
        return workThreadPool;
    }

    public static NioEventLoopGroup getOrCreateIoThreadPool(int threadNumber) {
        if (ioEventLoopGroup == null) {
            ioEventLoopGroupLock.lock();
            try {
                if (ioEventLoopGroup == null) {
                    ioEventLoopGroup = new NioEventLoopGroup(threadNumber
                            , new NamedThreadFactory(ioEventLoopGroupName, false));
                }
            } finally {
                ioEventLoopGroupLock.unlock();
            }
        }
        return ioEventLoopGroup;
    }

}
