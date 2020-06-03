package com.jsj.rpc.protocol.standard;

import com.jsj.rpc.ChannelInfo;
import com.jsj.rpc.RpcException;
import com.jsj.rpc.RpcFuture;
import com.jsj.rpc.RpcMethodDetail;
import com.jsj.rpc.protocol.*;
import com.jsj.rpc.protocol.exception.BadSchemaException;
import com.jsj.rpc.protocol.exception.NotEnoughDataException;
import com.jsj.rpc.server.ServiceManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
    private ServiceManager serviceManager = ServiceManager.getInstance();

    public RpcProtocol() {
    }

    public RpcProtocol(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public Packet createPacket(ByteBuf data) {
        return new RpcPacket(data);
    }

    @Override
    public Packet createPacket(byte[] data) {
        ByteBuf byteBuf = Unpooled.buffer(data.length);
        byteBuf.writeBytes(data);
        return createPacket(byteBuf);
    }

    @Override
    public Request createRequest() {
        return new RpcRequest();
    }

    @Override
    public Response createResponse() {
        return new RpcResponse();
    }

    @Override
    public <T> RpcFuture<T> createRpcFuture(Request request) {
        return new RpcFuture<>(request);
    }

    @Override
    public Packet parseHeaderAndPackageContent(ByteBuf in) throws BadSchemaException, NotEnoughDataException {
        if (in.readableBytes() < FIXED_HEADER_LEN) {
            throw new NotEnoughDataException();
        }
        in.markReaderIndex();
        byte magicNumber = in.readByte();
        int bodyLength = in.readInt();
        if (in.readableBytes() < bodyLength) {
            in.resetReaderIndex();
            throw new NotEnoughDataException();
        } else if (in.readableBytes() > bodyLength) {
            in.resetReaderIndex();
            throw new BadSchemaException();
        }

        ByteBuf bodyBuf = in.readRetainedSlice(bodyLength);
        return new RpcPacket(bodyBuf);
    }

    @Override
    public ByteBuf encodePacket(Packet packet) throws Exception {
        ByteBuf bodyBuf = packet == null ? null : packet.getBody();
        int bodyLength = bodyBuf == null ? 0 : bodyBuf.readableBytes();

        CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer(2);
        compositeByteBuf.addComponent(true, createHeaderBuf(bodyLength));
        if (bodyBuf != null) {
            compositeByteBuf.addComponent(true, bodyBuf);
        }
        return compositeByteBuf;
    }

    private ByteBuf createHeaderBuf(int bodyLength) {
        ByteBuf headBuf = Unpooled.buffer(FIXED_HEADER_LEN);
        //protocol version
        headBuf.writeByte(MAGIC_NUM);
        //body length
        headBuf.writeInt(bodyLength);
        return headBuf;
    }

    @Override
    public Request decodeAsRequest(Packet packet) throws Exception {
        RpcMeta.RequestMeta requestMeta = RpcMeta.RequestMeta
                .parseFrom(packet.getBody().nioBuffer());

        RpcMethodDetail methodDetail =
                serviceManager.getService(requestMeta.getServiceName(), requestMeta.getMethodName());
        if (methodDetail == null) {
            String errMsg = String.format("rpc interface name: %s, method name: %s"
                    , requestMeta.getServiceName(), requestMeta.getMethodName());
            throw new NoSuchMethodException(errMsg);
        }

        //参数类型转换
        Class[] paramTypes = methodDetail.getParamTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Object param = requestMeta.getParams(i).unpack(paramTypes[i]);
            params[i] = param;
        }
        Request request = createRequest();
        request.setRequestId(requestMeta.getRequestId());
        request.setServiceName(requestMeta.getServiceName());
        request.setMethodName(requestMeta.getMethodName());
        request.setParams(params);
        request.setMethod(methodDetail.getMethod());
        request.setTarget(methodDetail.getTarget());
        return request;
    }

    @Override
    public Response decodeAsResponse(Packet packet, ChannelInfo channelInfo) throws Exception {
        RpcMeta.ResponseMeta responseMeta = RpcMeta.ResponseMeta
                .parseFrom(packet.getBody().nioBuffer());
        RpcFuture<?> rpcFuture = channelInfo.getAndRemoveRpcFuture(responseMeta.getRequestId());
        Request request = rpcFuture.getRequest();
        Class returnType = request.getMethod().getReturnType();

        Response response = createResponse();
        response.setRequestId(responseMeta.getRequestId());
        response.setRpcFuture(rpcFuture);
        if (responseMeta.getResult() != null) {
            response.setResult(responseMeta.getResult().unpack(returnType));
        }
        if (responseMeta.getErrMsg() != null && !"".equals(responseMeta.getErrMsg())) {
            response.setException(new RpcException(responseMeta.getErrMsg()));
        }
        return response;
    }
}
