package com.jsj.rpc;

import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 利用ThreadLocal，处理EventLoop中的RpcFuture获取删除操作，避免多余的加锁操作
 *
 * @author jsj
 * @date 2018-01-04
 */
public class ChannelDataHolder {
    private static int CHANNEL_INIT_SIZE = 8;
    private static ThreadLocal<Map<Channel, ChannelData>> CACHE = new ThreadLocal<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelDataHolder.class);

    private EventLoop eventLoop;

    public ChannelDataHolder(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    /**
     * 更新channel的最近请求接收时间,对应EventLoop线程执行
     *
     * @param channel
     */
    public static void updateChannel(Channel channel) {
        long lastReceive = System.currentTimeMillis();
        Map<Channel, ChannelData> cache = ChannelDataHolder.checkCache();
        ChannelData channelData = cache.get(channel);
        if (channelData == null) {
            channelData = new ChannelData(channel, lastReceive);
            cache.put(channel, channelData);
        }
        channelData.update(lastReceive);
        LOGGER.info("channel: {} updated.", channel);
    }

    /**
     * 当channel关闭时，清除eventLoop中channel对应的所有future，对应EventLoop线程执行
     *
     * @param channel
     */
    public static void removeChannel(Channel channel) {
        Map<Channel, ChannelData> cache = checkCache();
        cache.remove(channel);
    }

    /**
     * 调用异常时清除eventLoop中的future，EventLoop线程执行
     *
     * @param channel
     * @param id
     */
    public static DefaultRpcFuture removeFuture(Channel channel, Integer id) {
        Map<Channel, ChannelData> cache = checkCache();
        ChannelData channelData = cache.get(channel);
        if (channelData == null) {
            channelData = new ChannelData(channel, System.currentTimeMillis());
            cache.put(channel, channelData);
        }
        return channelData.removeRpcFuture(id);
    }

    /**
     * 判断channel是否过期,若过期则关闭并删除channel,EventLoop线程执行
     */
    public static boolean removeIfExpire(Channel channel, int channelAliveTime) {
        boolean removed = false;
        Map<Channel, ChannelData> cache = ChannelDataHolder.checkCache();
        ChannelData channelData = cache.get(channel);
        if (channelData == null) {
            return true;
        }
        if (channelData.expire(channelAliveTime)) {
            if (channel.isOpen()) {
                channel.close();
            }
            cache.remove(channel);
            removed = true;
            LOGGER.info("channel: {} expired and closed.", channel);
        }
        return removed;
    }

    /**
     * 在eventLoop的CACHE中添加future，调用线程执行
     *
     * @param channel
     * @param future
     */
    public void set(Channel channel, DefaultRpcFuture future) {
        this.eventLoop.execute(() -> {
            Map<Channel, ChannelData> cache = ChannelDataHolder.checkCache();
            ChannelData channelData = cache.get(channel);
            if (channelData == null) {
                channelData = new ChannelData(channel, System.currentTimeMillis());
                cache.put(channel, channelData);
            }
            channelData.putRpcFuture(future);
        });
    }

    /**
     * 调用异常时清除eventLoop中的future，调用线程执行
     *
     * @param channel
     * @param future
     */
    public void remove(Channel channel, DefaultRpcFuture future) {
        this.eventLoop.execute(() -> {
            Map<Channel, ChannelData> cache = ChannelDataHolder.checkCache();
            ChannelData channelData = cache.get(channel);
            if (channelData == null) {
                channelData = new ChannelData(channel, System.currentTimeMillis());
                cache.put(channel, channelData);
            }
            channelData.removeRpcFuture(future.getRequestId());
        });
    }

    /**
     * 清除eventLoop中所有缓存
     */
    public static void removeAll() {
        CACHE.remove();
    }

    private static Map<Channel, ChannelData> checkCache() {
        Map<Channel, ChannelData> cache = CACHE.get();
        if (cache == null) {
            cache = new HashMap<>(CHANNEL_INIT_SIZE);
            CACHE.set(cache);
        }
        return cache;
    }
}
