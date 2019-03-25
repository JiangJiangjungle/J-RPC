package com.jsj.rpc.codec;


import com.jsj.rpc.common.Body;
import com.jsj.rpc.common.Header;
import com.jsj.rpc.common.RpcRequest;
import com.jsj.rpc.common.RpcResponse;
import com.jsj.rpc.util.MessageUtil;
import com.jsj.rpc.util.SerializationUtil;
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
public class RpcDecoder extends ByteToMessageDecoder {


    private final CodeStrategy strategy;

    public static int DEFAULT_LENGTH_FIELD_OFFSET = 4;

    public RpcDecoder(CodeStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < DEFAULT_LENGTH_FIELD_OFFSET) {
            return;
        }
        //标记当前的readIndex的位置
        in.markReaderIndex();
        int dataLength = in.readInt();
        //消息体长度如果小于传送过来的消息长度，则resetReaderIndex，把readIndex重置到mark的地方
        if (in.readableBytes() < dataLength + 1) {
            in.resetReaderIndex();
            return;
        }
        Header header = new Header(dataLength, in.readByte());
        if (dataLength == 0) {
            out.add(MessageUtil.buildNormalMessage(header, null));
        } else {
            byte[] data = new byte[dataLength];
            in.readBytes(data);
            Class cls = Header.RPC_REQUEST == header.getType() ? RpcRequest.class : RpcResponse.class;
            out.add(MessageUtil.buildNormalMessage(header, (Body) SerializationUtil.deserialize(data, cls, this.strategy)));
        }
    }
}
