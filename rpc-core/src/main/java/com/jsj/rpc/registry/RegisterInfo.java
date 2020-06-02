package com.jsj.rpc.registry;

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
public class RegisterInfo {
    private String interfaceName;
    private String host;
    private int port;

    public RegisterInfo(String interfaceName, String host, int port) {
        this.interfaceName = interfaceName;
        this.host = host;
        this.port = port;
    }
}
