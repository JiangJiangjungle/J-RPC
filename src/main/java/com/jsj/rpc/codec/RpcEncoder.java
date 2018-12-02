package com.jsj.rpc.codec;


import com.jsj.rpc.common.Body;
import com.jsj.rpc.common.Header;
import com.jsj.rpc.common.RpcRequest;
import com.jsj.rpc.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC 编码器
 *
 * @author jsj
 * @date 2018-11-6
 */
public class RpcEncoder extends MessageToByteEncoder {

    private final CodeStrategy strategy;

    public RpcEncoder(CodeStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (in instanceof Header) {
            Header header = (Header) in;
            out.writeInt(header.getDataLength());
            out.writeByte(header.getType());
        } else {
            byte[] data = SerializationUtil.serialize((Body) in, strategy);
            out.writeInt(data.length);
            out.writeByte(in instanceof RpcRequest ? Header.RPC_REQUEST : Header.RPC_RESPONSE);
            out.writeBytes(data);
        }
    }
}