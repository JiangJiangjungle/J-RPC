package com.jsj.nettyrpc.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

    /**
     * connections
     */
    private CopyOnWriteArrayList<Connection> conns = new CopyOnWriteArrayList<Connection>();

    /**
     * add a connection
     *
     * @param connection
     */
    public void add(Connection connection) {
        if (null == connection) {
            return;
        }
        this.conns.addIfAbsent(connection);
    }

    /**
     * check weather a connection already added
     *
     * @param connection
     * @return
     */
    public boolean contains(Connection connection) {
        return this.conns.contains(connection);
    }

    /**
     * get a connection
     *
     * @return
     */
    public Connection get() {
        if (null != this.conns) {
            List<Connection> snapshot = new ArrayList<Connection>(this.conns);
            if (snapshot.size() > 0) {
                //随机选取
                return snapshot.get(ThreadLocalRandom.current().nextInt(snapshot.size()));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * removeAndTryClose a connection
     *
     * @param connection
     */
    public void removeAndTryClose(Connection connection) {
        if (null == connection) {
            return;
        }
        this.conns.remove(connection);
        connection.close();
    }

    /**
     * connection pool size
     *
     * @return
     */
    public int size() {
        return this.conns.size();
    }

}
