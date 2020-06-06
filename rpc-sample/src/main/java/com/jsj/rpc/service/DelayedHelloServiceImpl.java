package com.jsj.rpc.service;

import com.jsj.rpc.User;

/**
 * @author jiangshenjie
 */
public class DelayedHelloServiceImpl implements DelayedHelloService {

    HelloService helloService;

    public DelayedHelloServiceImpl(HelloService helloService) {
        this.helloService = helloService;
    }

    @Override
    public User.UserDetail hello(User.UserInfo userInfo) {
        try {
            Thread.sleep(3000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return helloService.hello(userInfo);
    }
}
