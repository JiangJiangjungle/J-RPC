package com.jsj.rpc.codec;


import com.jsj.rpc.protocol.Body;
import com.jsj.rpc.protocol.Header;
import com.jsj.rpc.util.MessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * RPC 解码器
 *
 * @author jsj
 * @date 2018-11-6
 */
public class BaseDecoder extends ByteToMessageDecoder {

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < Header.PROTOCOL_HEADER_BYTES) {
            return;
        }
        //标记当前的readIndex的位置
        in.markReaderIndex();
        //解析首部，获取Header对象
        Header header = MessageUtil.byteBufTransToHeader(in);
        //消息体长度如果小于传送过来的消息长度，则resetReaderIndex，把readIndex重置到mark的地方
        if (header.bodyLength() != 0 && in.readableBytes() < header.bodyLength()) {
            in.resetReaderIndex();
            return;
        }
        //解析并反序列化得到Body对象
        Body body = MessageUtil.byteBufTransToBody(header, in);
        // out添加封装的Message对象
        out.add(MessageUtil.createMessage(header, body));
    }
}
