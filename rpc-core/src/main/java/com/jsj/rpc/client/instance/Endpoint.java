package com.jsj.rpc.client.instance;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * @author jiangshenjie
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class Endpoint {
    private String ip;
    private int port;

    public Endpoint(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Endpoint endpoint = (Endpoint) o;
        return port == endpoint.port &&
                ip.equals(endpoint.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
