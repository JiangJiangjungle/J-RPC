package com.jsj.rpc.registry;

public class Address {
    private final String ip;
    private final int port;

    public Address(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}
