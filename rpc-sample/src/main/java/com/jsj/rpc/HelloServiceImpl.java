package com.jsj.rpc;

/**
 * @author jiangshenjie
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public User.UserDetail hello(User.UserInfo userInfo) {
        User.UserDetail.Builder builder = User.UserDetail.newBuilder();
        builder.setPassword("123456");
        builder.setUserInfo(userInfo);
        return builder.build();
    }
}
