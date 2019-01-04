package com.jsj.rpc.client;

import com.jsj.rpc.common.RpcFuture;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;

import java.util.HashMap;
import java.util.Map;

/**
 * 利用ThreadLocal，处理EventLoop中的RpcFuture获取删除操作，避免多余的加锁操作
 *
 * @author jsj
 * @date 2018-01-04
 */
public class RpcFutureHolder {

    public static int FUTURE_MAP_INIT_SIZE = 128;
    public static int CHANNEL_INIT_SIZE = 4;

    public static ThreadLocal<Map<Channel, Map<Integer, RpcFuture>>> CACHE = new ThreadLocal<>();

    private EventLoop eventLoop;

    public RpcFutureHolder(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    /**
     * 在eventLoop的CACHE中添加future
     *
     * @param channel
     * @param future
     */
    public void set(Channel channel, RpcFuture future) {
        this.eventLoop.execute(() -> {
            Map<Channel, Map<Integer, RpcFuture>> cache = RpcFutureHolder.checkCache();
            Map<Integer, RpcFuture> futureMap = cache.computeIfAbsent(channel, k -> new HashMap<>(FUTURE_MAP_INIT_SIZE));
            futureMap.put(future.getRequestId(), future);
        });
    }

    /**
     * 调用异常时清除eventLoop中的future
     *
     * @param channel
     * @param future
     */
    public void remove(Channel channel, RpcFuture future) {
        this.eventLoop.execute(() -> {
            Map<Channel, Map<Integer, RpcFuture>> cache = RpcFutureHolder.checkCache();
            Map<Integer, RpcFuture> futureMap = cache.get(channel);
            if (futureMap == null) {
                cache.put(channel, new HashMap<>(FUTURE_MAP_INIT_SIZE));
            } else {
                futureMap.remove(future.getRequestId());
            }
        });
    }

    /**
     * 调用异常时清除eventLoop中的future
     *
     * @param channel
     * @param id
     */
    public static RpcFuture removeFuture(Channel channel, Integer id) {
        Map<Channel, Map<Integer, RpcFuture>> cache = checkCache();
        Map<Integer, RpcFuture> futureMap = cache.get(channel);
        if (futureMap == null) {
            cache.put(channel, new HashMap<>(FUTURE_MAP_INIT_SIZE));
        } else {
            return futureMap.remove(id);
        }
        return null;
    }

    /**
     * 当channel关闭时，清除eventLoop中channel对应的所有future
     *
     * @param channel
     */
    public static void removeChannel(Channel channel) {
        Map<Channel, Map<Integer, RpcFuture>> cache = checkCache();
        cache.remove(channel);
    }

    /**
     * 清除eventLoop中所有缓存
     */
    public static void removeAll() {
        CACHE.remove();
    }

    private static Map<Channel, Map<Integer, RpcFuture>> checkCache() {
        Map<Channel, Map<Integer, RpcFuture>> cache = CACHE.get();
        if (cache == null) {
            cache = new HashMap<>(CHANNEL_INIT_SIZE);
            CACHE.set(cache);
        }
        return cache;
    }
}
