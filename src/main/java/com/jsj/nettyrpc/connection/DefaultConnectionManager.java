package com.jsj.nettyrpc.connection;

import com.jsj.nettyrpc.codec.CodeC;
import com.jsj.nettyrpc.exception.RemotingException;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * netty 连接管理器
 */
public class DefaultConnectionManager implements ConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionManager.class);
    /**
     * connection factory
     */
    protected ConnectionFactory connectionFactory;

    private ChannelHandler channelHandler;
    private CodeC codeC;

    private ConcurrentHashMap<String, ConnectionPool> connectionPoolMap = new ConcurrentHashMap<>();

    public DefaultConnectionManager(ChannelHandler channelHandler, CodeC codeC) {
        this.channelHandler = channelHandler;
        this.codeC = codeC;
    }

    @Override
    public void init() {
        connectionFactory = new DefaultConnectionFactory(channelHandler, codeC);
    }

    @Override
    public void add(Connection connection) {
        String poolKey = connection.getPoolKey();
        ConnectionPool connectionPool = connectionPoolMap.get(poolKey);
        connectionPool.add(connection);
    }

    @Override
    public Connection get(String poolKey) {
        ConnectionPool connectionPool = connectionPoolMap.get(poolKey);
        return connectionPool.get();
    }

    @Override
    public void remove(Connection connection) {
        String poolKey = connection.getPoolKey();
        ConnectionPool connectionPool = connectionPoolMap.get(poolKey);
        connectionPool.removeAndTryClose(connection);
    }

    @Override
    public Connection create(String ip, int port, int connectTimeout) throws RemotingException {
        Connection conn;
        try {
            conn = this.connectionFactory.createConnection(ip, port, connectTimeout);
        } catch (Exception e) {
            throw new RemotingException("Create connection failed. The address is " + ip);
        }
        return conn;
    }
}
