package com.jsj.nettyrpc.client;


import com.jsj.nettyrpc.common.RpcRequest;
import com.jsj.nettyrpc.common.RpcResponse;
import com.jsj.nettyrpc.codec.RpcDecoder;
import com.jsj.nettyrpc.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * RPC 客户端（用于发送 RPC 请求）
 *
 * @author jsj
 * @date 2018-10-10
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private final String host;
    private final int port;

    private RpcResponse response;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        this.response = response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("api caught exception", cause);
    }

    public RpcResponse send(RpcRequest request) throws Exception {
        //配置客户端 NIO 线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 创建并初始化 Netty 客户端 Bootstrap 对象
            Bootstrap bootstrap = this.initBootstrap(group);
            // 连接 RPC 服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            // 写入 RPC 请求数据并关闭连接
            Channel channel = future.channel();
            channel.writeAndFlush(request).sync();
            // 等待客户端链路关闭
            channel.closeFuture().sync();
            // 返回 RPC 响应对象
            return response;
        } finally {
            //优雅退出，释放 NIO 线程组
            group.shutdownGracefully();
        }
    }

    /**
     * 创建并初始化 Netty 客户端辅助启动对象 Bootstrap
     *
     * @param group
     * @return
     */
    private Bootstrap initBootstrap(EventLoopGroup group) {
        return new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        //编码 RPC 请求
                        pipeline.addLast(new RpcEncoder(RpcRequest.class))
                                //解码 RPC 响应
                                .addLast(new RpcDecoder(RpcResponse.class))
                                //处理 RPC 响应
                                .addLast(RpcClient.this);
                    }
                });
    }
}
