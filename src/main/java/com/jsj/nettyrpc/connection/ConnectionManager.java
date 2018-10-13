package com.jsj.nettyrpc.connection;

import com.jsj.nettyrpc.exception.RemotingException;

public interface ConnectionManager {

    /**
     * init
     */
    void init();

    /**
     * Add a connection to {@link ConnectionPool}.
     * If it contains multiple pool keys, this connection will be added to multiple {@link ConnectionPool} too.
     *
     * @param connection an available connection, you should {@link #check(Connection)} this connection before add
     */
    void add(Connection connection);

    /**
     * Get a connection from {@link ConnectionPool} with the specified poolKey.
     *
     * @param poolKey unique key of a {@link ConnectionPool}
     * @return a {@link Connection} selected by {@link ConnectionSelectStrategy}<br>
     * or return {@code null} if there is no {@link ConnectionPool} mapping with poolKey<br>
     * or return {@code null} if there is no {@link Connection} in {@link ConnectionPool}.
     */
    Connection get(String poolKey);

    /**
     * Remove a {@link Connection} from all {@link ConnectionPool} with the poolKeys in {@link Connection}, and close it.
     */
    void remove(Connection connection);

    /**
     * Create a connection using specified ip and port.
     *
     * @param ip             connect ip, e.g. 127.0.0.1
     * @param port           connect port, e.g. 1111
     * @param connectTimeout an int connect timeout value
     * @return the created {@link Connection}
     */
    Connection create(String ip, int port, int connectTimeout) throws RemotingException;


}
