# My-NettyRPC

#### 一个基于Netty实现的轻量级rpc框架

# 通信框架
- #### Netty

# 服务注册与发现中心
- #### ZooKeeper
  在客户端维护了一个<ServiceName,Channel>的本地缓存。查询服务时，首先检查对应服务是否维护了有效长连接，若存在连接则直接发送请求，以避免每次调用都需要zookeeper查询。

# 序列化

- #### Protostuff
- #### JSON
- #### JDK Serializable

# RPC调用

- #### 同步调用
- #### 异步（Future）调用

# 可靠连接

- #### 简单的自定义通信协议

  <table>
       <tr>
          <th colspan="7" rowspan="1" align="center">protocol_headr(7 byte)</th>
          <th colspan="1" rowspan="1" align="center">protocol_body</th>
      </tr>
      <tr>
          <td colspan="1" rowspan="1" align="center">protocol_code (1 byte)</td>
          <td colspan="1" rowspan="1" align="center">message_type (1 byte)</td>
          <td colspan="1" rowspan="1" align="center">serialization_type (1 byte)</td>
          <td colspan="4" rowspan="1" align="center">body_length (4 byte)</td>
          <td colspan="1" rowspan="1" align="center">...</td>
      </tr>
  </table>

- #### 心跳检测机制

  客户端单向发送心跳请求，由服务端返回响应

- #### 断线重连机制

  客户端负责定时执行任务，重新建立与服务端的连接，直到达到最大重试次数

- #### 超时处理

  考虑远程服务的调用无响应问题，增加了超时处理逻辑

# 本地测试

- #### 测试demo

  hello-client,不同线程参数配置下，各重复测试500次。

- #### 测试结果
<table>
    <tr>
        <th rowspan="2">线程数</th>
        <th colspan="2">JDK</th>
        <th colspan="2">JSON</th>
        <th colspan="2">PROTOSTUFF</th>
    </tr>
    <tr>
        <td>同步调用</td>
        <td>异步调用</td>
        <td>同步调用</td>
        <td>异步调用</td>
        <td>同步调用</td>
        <td>异步调用</td>
    </tr>
    <tr>
        <td>100</td>
        <td>26ms</td>
        <td>26ms</td>
        <td>19ms</td>
        <td>20ms</td>
        <td>19ms</td>
        <td>21ms</td>
    </tr>
    <tr>
        <td>200</td>
        <td>54ms</td>
        <td>57ms</td>
        <td>41ms</td>
        <td>41ms</td>
        <td>42ms</td>
        <td>41ms</td>
    </tr>
     <tr>
        <td>500</td>
        <td>145ms</td>
        <td>149ms</td>
        <td>102ms</td>
        <td>104ms</td>
        <td>102ms</td>
        <td>104ms</td>
    </tr>
     <tr>
        <td>1000</td>
        <td>293ms</td>
        <td>304ms</td>
        <td>214ms</td>
        <td>210ms</td>
        <td>207ms</td>
        <td>204ms</td>
    </tr>   
    <tr>
        <td>2000</td>
        <td>586ms</td>
        <td>609ms</td>
        <td>476ms</td>
        <td>444ms</td>
        <td>435ms</td>
        <td>427ms</td>
    </tr>  
</table>
