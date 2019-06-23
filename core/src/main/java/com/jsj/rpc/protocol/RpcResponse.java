package com.jsj.rpc.protocol;

import java.io.Serializable;

/**
 * 封装 RPC 响应
 *
 * @author jsj
 * @date 2018-10-9
 */
public class RpcResponse implements Body, Serializable {
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

    public RpcResponse(Integer requestId, Object serviceResult, String errorMsg) {
        this.requestId = requestId;
        this.serviceResult = serviceResult;
        this.errorMsg = errorMsg;
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

    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestId=" + requestId +
                ", serviceResult=" + serviceResult +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
