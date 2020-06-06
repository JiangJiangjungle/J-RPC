package com.jsj.rpc.service;

import com.jsj.rpc.User;

/**
 * @author jiangshenjie
 */
public interface DelayedHelloService {
    User.UserDetail hello(User.UserInfo userInfo);
}
