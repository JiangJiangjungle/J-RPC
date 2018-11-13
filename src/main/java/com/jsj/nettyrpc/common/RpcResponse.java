package com.jsj.nettyrpc.common;

import java.io.Serializable;

/**
 * 封装 RPC 响应
 *
 * @author jsj
 * @date 2018-10-9
 */
public class RpcResponse  implements Serializable {

    private boolean heartBeat;
    /**
     * 请求id
     */
    private Integer requestId;
    /**
     * RPC响应对象
     */
    private Object serviceResult;
    /**
     * 错误信息
     */
    private String errorMsg;

    public RpcResponse() {
    }

    public RpcResponse(boolean heartBeat, Integer requestId, Object serviceResult, String errorMsg) {
        this.heartBeat = heartBeat;
        this.requestId = requestId;
        this.serviceResult = serviceResult;
        this.errorMsg = errorMsg;
    }

    public boolean isHeartBeat() {
        return heartBeat;
    }

    public void setHeartBeat(boolean heartBeat) {
        this.heartBeat = heartBeat;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Object getServiceResult() {
        return serviceResult;
    }

    public void setServiceResult(Object serviceResult) {
        this.serviceResult = serviceResult;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
