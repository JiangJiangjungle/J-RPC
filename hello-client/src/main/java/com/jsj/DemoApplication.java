package com.jsj;


import com.jsj.rpc.NamedThreadFactory;
import com.jsj.rpc.client.RpcProxy;
import com.jsj.rpc.codec.CodeStrategy;
import com.jsj.rpc.common.RpcFuture;
import com.jsj.rpc.common.RpcResponse;
import com.jsj.rpc.registry.ServiceDiscovery;
import com.jsj.rpc.registry.impl.ZookeeperDiscovery;
import com.jsj.task.FutureTestTask;
import com.jsj.service.HelloService;
import com.jsj.task.SyncTestTask;

import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.concurrent.*;

public class DemoApplication {

    static String IP = "119.23.204.78";
    static String PORT = "2181";
    static ServiceDiscovery serviceDiscovery = new ZookeeperDiscovery(IP + ":" + PORT);

    static RpcProxy rpcProxy = new RpcProxy(serviceDiscovery,CodeStrategy.PROTO_STUFF);
    static HelloService helloService = rpcProxy.getService(HelloService.class);


    public static long testSync(int threads, ExecutorService executorService) throws Exception {
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

    public static long testNoSync(int threads, ExecutorService executorService) throws Exception {
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
        int threads = 1000;
        ExecutorService executorService = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(threads * 10), new NamedThreadFactory());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        executorService.submit(new FutureTestTask(rpcProxy, countDownLatch));
        Thread.sleep(1000);
        System.out.println("RPC服务远程节点信息已缓存...");
        int repeat = 500;
        long sum = 0L;
        long count;
        for (int i = 1; i <= repeat; i++) {
            count = testSync(threads, executorService);
            sum += count;
            System.out.println("第" + i + "次测试，平均响应时间：" + count / (threads) + " ms.");
        }
        System.out.println("------测试结束--------");
        System.out.println("在线程数为：" + threads + " 的条件下，累计平均响应时间：" + sum / (repeat * threads) + " ms.");
    }
}
