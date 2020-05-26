package com.jsj.rpc.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;

public class CompositeByteBufTest {
    @Test
    public void testRead() {
        ByteBuf buf1 = Unpooled.directBuffer(64);
        ByteBuf buf2 = Unpooled.directBuffer(64);
        buf1.writeBytes("Hello world\n".getBytes());
        buf2.writeBytes("and you.\n".getBytes());
        CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer();
        compositeByteBuf.addComponent(true, buf1);
        compositeByteBuf.addComponent(true, buf2);
        Assert.assertTrue(compositeByteBuf.readableBytes() > 0);
        byte[] bytes = new byte[compositeByteBuf.readableBytes()];
        compositeByteBuf.readBytes(bytes);
        System.out.println(new String(bytes, Charset.defaultCharset()));
        compositeByteBuf.release();
        Assert.assertEquals(0, compositeByteBuf.refCnt());
    }
}
