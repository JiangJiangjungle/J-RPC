package com.jsj.rpc.protocol;

public interface Message {

    boolean emptyBody();

    Header getHeader();

    Body getBody();
}
