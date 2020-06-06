package com.jsj.rpc.client;

import com.jsj.rpc.util.NamedThreadFactory;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
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

    private static ScheduledThreadPoolExecutor scheduledThreadPool;
    private static String scheduledThreadPoolName = "rpc-client-scheduled-thread";
    private static Lock scheduledThreadPoolLock = new ReentrantLock();

    public static ThreadPoolExecutor getOrCreateWorkThreadPool(int threadNumber, int blockingQueueSize) {
        if (workThreadPool == null || workThreadPool.isShutdown()) {
            workThreadPoolLock.lock();
            try {
                if (workThreadPool == null || workThreadPool.isShutdown()) {
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
        if (ioEventLoopGroup == null || ioEventLoopGroup.isShutdown()) {
            ioEventLoopGroupLock.lock();
            try {
                if (ioEventLoopGroup == null || ioEventLoopGroup.isShutdown()) {
                    ioEventLoopGroup = new NioEventLoopGroup(threadNumber
                            , new NamedThreadFactory(ioEventLoopGroupName, false));
                }
            } finally {
                ioEventLoopGroupLock.unlock();
            }
        }
        return ioEventLoopGroup;
    }

    public static ScheduledThreadPoolExecutor getOrCreateScheduledThreadPool(int threadNumber) {
        if (scheduledThreadPool == null || scheduledThreadPool.isShutdown()) {
            scheduledThreadPoolLock.lock();
            try {
                if (scheduledThreadPool == null || scheduledThreadPool.isShutdown()) {
                    scheduledThreadPool = new ScheduledThreadPoolExecutor(threadNumber
                            , new NamedThreadFactory(scheduledThreadPoolName, false));
                }
            } finally {
                scheduledThreadPoolLock.unlock();
            }
        }
        return scheduledThreadPool;
    }

    public static void close() {
        if (workThreadPool != null) {
            workThreadPool.shutdown();
        }
        if (ioEventLoopGroup != null) {
            ioEventLoopGroup.shutdownGracefully().awaitUninterruptibly();
        }
        if (scheduledThreadPool != null) {
            scheduledThreadPool.shutdown();
        }
    }
}
