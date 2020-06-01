package com.jsj.rpc.protobuf;

import com.google.protobuf.Any;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class ProtobufTest {

    @Test
    public void testSerialize() throws Exception {
        User.UserInfoProto.Builder userInfoBuilder = User.UserInfoProto.newBuilder();
        userInfoBuilder.setAge(10);
        userInfoBuilder.setName("wsh");
        userInfoBuilder.setId(1L);

        Generic.GenericInfo.Builder genericInfoBuilder = Generic.GenericInfo.newBuilder();
        genericInfoBuilder.addDetails(Any.pack(userInfoBuilder.build()));
        Generic.GenericInfo genericInfo = genericInfoBuilder.build();

        byte[] bytes = genericInfo.toByteArray();
        log.debug("序列化GenericInfo: {}", bytes);
        Generic.GenericInfo rebuilt = Generic.GenericInfo.parseFrom(bytes);
        User.UserInfoProto userInfoProto = rebuilt.getDetails(0).unpack(User.UserInfoProto.class);
        log.debug("反序列化UserInfoProto: {}", userInfoProto.toString());
    }
}
