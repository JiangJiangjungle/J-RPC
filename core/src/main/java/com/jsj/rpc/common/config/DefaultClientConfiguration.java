package com.jsj.rpc.common.config;

import com.jsj.rpc.common.codec.CodeC;
import com.jsj.rpc.common.codec.DefaultCodeC;
import com.jsj.rpc.common.serializer.SerializerTypeEnum;

/**
 * 客户端连接配置
 *
 * @author jiangshenjie
 */
public class DefaultClientConfiguration {
    /**
     * 客户端断线重连的最大重试次数
     */
    private Integer maxRetries = 10;

    /**
     * 连接超时，单位：ms
     */
    private Integer connectTimeout = 2000;

    /**
     * 是否禁用NAGLE算法
     */
    private Boolean tcpNoDelay = true;

    /**
     * 异步调用超时，单位：ms
     */
    private Integer rpcRequestTimeout = 3000;

    /**
     * 读操作等待 单位: ms
     */
    private int readIdle = 20000;
    /**
     * 写操作等待，单位：ms
     */
    private int writeIdle = 10000;

    private SerializerTypeEnum serializerTypeEnum = SerializerTypeEnum.DEFAULT;

    /**
     * 编码方案
     */
    private CodeC codeC = DefaultCodeC.getInstance();

    public DefaultClientConfiguration() {
    }

    public DefaultClientConfiguration(Integer maxRetries, Integer connectTimeout, Boolean tcpNoDelay, Integer rpcRequestTimeout, int readIdle, int writeIdle, SerializerTypeEnum serializerTypeEnum) {
        this.maxRetries = maxRetries;
        this.connectTimeout = connectTimeout;
        this.tcpNoDelay = tcpNoDelay;
        this.rpcRequestTimeout = rpcRequestTimeout;
        this.readIdle = readIdle;
        this.writeIdle = writeIdle;
        this.serializerTypeEnum = serializerTypeEnum;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Boolean getTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(Boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public Integer getRpcRequestTimeout() {
        return rpcRequestTimeout;
    }

    public void setRpcRequestTimeout(Integer rpcRequestTimeout) {
        this.rpcRequestTimeout = rpcRequestTimeout;
    }

    public int getReadIdle() {
        return readIdle;
    }

    public void setReadIdle(int readIdle) {
        this.readIdle = readIdle;
    }

    public int getWriteIdle() {
        return writeIdle;
    }

    public void setWriteIdle(int writeIdle) {
        this.writeIdle = writeIdle;
    }

    public CodeC getCodeC() {
        return codeC;
    }

    public void setCodeC(CodeC codeC) {
        this.codeC = codeC;
    }

    public SerializerTypeEnum getSerializerTypeEnum() {
        return serializerTypeEnum;
    }

    public void setSerializerTypeEnum(SerializerTypeEnum serializerTypeEnum) {
        this.serializerTypeEnum = serializerTypeEnum;
    }
}
