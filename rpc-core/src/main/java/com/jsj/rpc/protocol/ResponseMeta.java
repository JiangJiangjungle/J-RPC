package com.jsj.rpc.protocol;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jiangshenjie
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ResponseMeta {
    private Long requestId;
    private Object result;
    private String errorMessage;
}
