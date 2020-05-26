package com.jsj.rpc.protocol;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jiangshenjie
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class RequestMeta {
    private Long requestId;
    private String serviceName;
    private String methodName;
    private Object[] params;
}
