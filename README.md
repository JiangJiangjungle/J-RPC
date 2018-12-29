# Netty-rpc

一个基于Netty实现的轻量级rpc框架

### 通信框架
 - **Netty**

### 服务注册与发现中心
 - **ZooKeeper**

在客户端维护了一个<ServiceName,Channel>的本地缓存。
查询服务时，首先检查对应服务是否维护了有效长连接，若存在连接则直接发送请求，以避免每次调用都需要zookeeper查询。

### 序列化
- **Protostuff**
- **JSON**
- **JDK Serializable**

### RPC调用
- **同步调用**
- **异步（Future）调用**

### 可靠连接
- **简单的自定义通信协议**

| 数据长度（4byte） | 消息类型（1byte） | 数据(不定长) |
| :-------- | --------:| :--: |

- **心跳检测机制**

客户端单向发送心跳请求，由服务端返回响应

- **断线重连机制**

客户端负责定时执行任务，重新建立与服务端的连接，直到达到最大重试次数

- **超时处理**

考虑远程服务的调用无响应问题，增加了超时处理逻辑

### 本地测试
- **测试工具**

Jmeter-5.0

- **并发参数设置**

![Threads](https://github.com/JiangJiangjungle/Netty-rpc/figures/Threads.png)
- **测试结果**

![Result](https://github.com/JiangJiangjungle/Netty-rpc/figures/Result.png)
