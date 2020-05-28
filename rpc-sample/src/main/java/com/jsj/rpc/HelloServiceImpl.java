package com.jsj.rpc;

/**
 * @author jiangshenjie
 */
public class HelloServiceImpl implements HelloService{

    @Override
    public String hello(String name) {
        return String.format("Hello %s!",name);
    }
}
