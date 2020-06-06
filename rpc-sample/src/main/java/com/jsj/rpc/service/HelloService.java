package com.jsj.rpc.service;

import com.jsj.rpc.User;

public interface HelloService {
    User.UserDetail hello(User.UserInfo userInfo);
}
