package com.jsj.rpc.protocol;

import com.jsj.rpc.RpcInvokeException;
import com.jsj.rpc.RpcMethodDetail;
import com.jsj.rpc.protocol.exception.BadSchemaException;
import com.jsj.rpc.protocol.exception.NotEnoughDataException;
import com.jsj.rpc.serializer.JsonSerializer;
import com.jsj.rpc.serializer.SerializeException;
import com.jsj.rpc.serializer.Serializer;
import com.jsj.rpc.server.ServiceManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * header：magic_num(1 byte) | body_length(4 byte)
 * body: msg content
 *
 * @author jiangshenjie
 */
@Slf4j
@Setter
public class RpcProtocol implements Protocol {
    private static int FIXED_HEADER_LEN = 5;
    /**
     * 默认协议版本号(1 byte)
     */
    private static byte MAGIC_NUM = (byte) 0x00;
    /**
     * 序列化/反序列化
     */
    private Serializer serializer = new JsonSerializer();
    private ServiceManager serviceManager = ServiceManager.getInstance();

    public RpcProtocol() {
    }

    public RpcProtocol(Serializer serializer, ServiceManager serviceManager) {
        this.serializer = serializer;
        this.serviceManager = serviceManager;
    }

    @Override
    public ByteBuf encodeMsg(Object message) throws SerializeException {
        CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer(2);
        ByteBuf headBuf = Unpooled.buffer(FIXED_HEADER_LEN);
        //protocol version
        headBuf.writeByte(MAGIC_NUM);
        if (message == null) {
            //body length
            headBuf.writeInt(0);
            compositeByteBuf.addComponent(true, headBuf);
        } else {
            ByteBuf bodyBuf = serializer.serialize(message);
            //body length
            headBuf.writeInt(bodyBuf.readableBytes());
            compositeByteBuf.addComponent(true, headBuf);
            compositeByteBuf.addComponent(true, bodyBuf);
        }
        return compositeByteBuf;
    }

    public ByteBuf decodeContent(ByteBuf in) throws NotEnoughDataException {
        if (in.readableBytes() < FIXED_HEADER_LEN) {
            throw new NotEnoughDataException();
        }
        in.markReaderIndex();
        byte magicNumber = in.readByte();
        int bodyLength = in.readInt();
        if (in.readableBytes() < bodyLength - 5) {
            in.resetReaderIndex();
            throw new NotEnoughDataException();
        }
        return in.readRetainedSlice(bodyLength);
    }

    @Override
    public RpcPacket parseHeader(ByteBuf in) throws BadSchemaException, NotEnoughDataException {
        return new RpcPacket(decodeContent(in));
    }

    @Override
    public RpcRequest decodeRequest(RpcPacket packet) throws Exception {
        RequestMeta meta = serializer.deSerialize(packet.getBody(), RequestMeta.class);
        RpcRequest request = new RpcRequest().values(meta);
        RpcMethodDetail methodDetail = serviceManager.getService(request.getServiceName(), request.getMethodName());
        if (methodDetail == null) {
            String errMsg = String.format("No such method, serviceName: %s, methodName: %s"
                    , request.getServiceName(), request.getMethodName());
            throw new Exception(errMsg);
        }
        request.setMethod(methodDetail.getMethod());
        request.setTarget(methodDetail.getTarget());
        return request;
    }

    @Override
    public RpcResponse decodeResponse(RpcPacket packet) throws SerializeException {
        ResponseMeta meta = serializer.deSerialize(packet.getBody(), ResponseMeta.class);
        RpcResponse response = new RpcResponse();
        response.setRequestId(meta.getRequestId());
        response.setResult(meta.getResult());
        if (meta.getErrorMessage() != null) {
            response.setException(new RpcInvokeException(meta.getErrorMessage()));
        }
        return response;
    }
}
