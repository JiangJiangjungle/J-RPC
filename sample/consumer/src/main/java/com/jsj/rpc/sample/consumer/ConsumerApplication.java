package com.jsj.rpc.sample.consumer;

import com.jsj.rpc.NamedThreadFactory;
import com.jsj.rpc.RpcProxy;
import com.jsj.rpc.codec.serializer.SerializerTypeEnum;
import com.jsj.rpc.registry.ServiceDiscovery;
import com.jsj.rpc.registry.impl.ZooKeeperRegistry;
import com.jsj.rpc.sample.consumer.task.FutureTestTask;
import com.jsj.rpc.sample.consumer.task.SyncTestTask;
import com.jsj.sample.api.service.HelloService;

import java.util.concurrent.*;

public class ConsumerApplication {
    static String IP = "139.9.77.156";
    static int PORT = 2181;

    public static long testSync(int threads, ExecutorService executorService,HelloService helloService) throws Exception {
        long sum = 0L;
        Future<Long>[] futureList = new Future[threads];
        CountDownLatch countDownLatch = new CountDownLatch(threads);
        for (int j = 0; j < threads; j++) {
            futureList[j] = executorService.submit(new SyncTestTask(helloService, countDownLatch));
        }
        for (int j = 0; j < threads; j++) {
            sum += futureList[+j].get();
        }
        return sum;
    }

    public static long testASync(int threads, ExecutorService executorService,RpcProxy rpcProxy) throws Exception {
        long sum = 0L;
        Future<Long>[] futureList = new Future[threads];
        CountDownLatch countDownLatch = new CountDownLatch(threads);
        for (int j = 0; j < threads; j++) {
            futureList[j] = executorService.submit(new FutureTestTask(rpcProxy, countDownLatch));
        }
        for (int j = 0; j < threads; j++) {
            sum += futureList[+j].get();
        }
        return sum;
    }

    public static void main(String[] args) throws Exception {
        //初始化
        ServiceDiscovery serviceDiscovery = new ZooKeeperRegistry(IP ,PORT);
        RpcProxy rpcProxy = new RpcProxy(serviceDiscovery, SerializerTypeEnum.JSON);
        HelloService helloService = rpcProxy.getService(HelloService.class);
        //线程设置
        int threads = 10;
        ExecutorService executorService = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(threads * 10), new NamedThreadFactory());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        executorService.submit(new FutureTestTask(rpcProxy, countDownLatch));
        System.out.println("RPC服务远程地址已本地缓存.");
        Thread.sleep(1000);
        int repeat = 500;
        long sum = 0L;
        long count;
        for (int i = 1; i <= repeat; i++) {
            count = testSync(threads, executorService,helloService);
            sum += count;
            System.out.println("第" + i + "次测试，平均响应时间：" + count / (threads) + " ms.");
        }
        System.out.println("------测试结束--------");
        System.out.println("在线程数为：" + threads + " 的条件下，累计平均响应时间：" + sum / (repeat * threads) + " ms.");
    }
}
