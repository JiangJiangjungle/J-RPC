package com.jsj.rpc.config;

import com.jsj.rpc.codec.CodeC;
import com.jsj.rpc.codec.DefaultCodeC;

/**
 * 服务端连接配置
 *
 * @author jiangshenjie
 */
public class DefaultServerConfiguration {
    /**
     * 未接收到心跳包后的TCP连接最大存活时间，单位：ms
     */
    private Integer channelAliveTime = 20000;

    /**
     * back_log
     */
    private Integer backLog = 1024;

    /**
     * 是否使用keepAlive模式
     */
    private Boolean keepAlive = true;

    /**
     * 绑定地址重用
     */
    private Boolean reUseAddr = true;

    /**
     * 编解码器
     */
    private CodeC codeC = DefaultCodeC.getInstance();


    public DefaultServerConfiguration() {
    }

    public Integer getChannelAliveTime() {
        return channelAliveTime;
    }

    public Integer getBackLog() {
        return backLog;
    }

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setChannelAliveTime(Integer channelAliveTime) {
        this.channelAliveTime = channelAliveTime;
    }

    public void setBackLog(Integer backLog) {
        this.backLog = backLog;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Boolean getReUseAddr() {
        return reUseAddr;
    }

    public void setReUseAddr(Boolean reUseAddr) {
        this.reUseAddr = reUseAddr;
    }

    public CodeC getCodeC() {
        return codeC;
    }

    public void setCodeC(CodeC codeC) {
        this.codeC = codeC;
    }
}