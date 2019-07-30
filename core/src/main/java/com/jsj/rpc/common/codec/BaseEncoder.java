package com.jsj.rpc.common.codec;


import com.jsj.rpc.common.message.Message;
import com.jsj.rpc.util.MessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC 编码器
 *
 * @author jsj
 * @date 2018-11-6
 */
public class BaseEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        MessageUtil.messageTransToByteBuf(message, byteBuf);
    }
}